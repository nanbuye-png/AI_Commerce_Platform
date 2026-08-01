package com.commerce.platform.user.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.user.dto.AuthResponse;
import com.commerce.platform.user.dto.LoginRequest;
import com.commerce.platform.user.dto.RegisterRequest;
import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 集成测试
 * 验证注册、登录、错误密码流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USERNAME = "test_user_auth";
    private static final String TEST_EMAIL = "test_auth@example.com";
    private static final String TEST_PASSWORD = "123456";

    @BeforeEach
    void setUp() {
        // 清理之前的测试用户
        userRepository.findByUsername(TEST_USERNAME).ifPresent(userRepository::delete);
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("用户注册 - 应成功创建用户并返回用户信息（不含密码哈希）")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setNickname("Test Auth User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();

        // 验证数据库中确实存在用户
        User savedUser = userRepository.findByUsername(TEST_USERNAME).orElse(null);
        assertNotNull(savedUser, "用户应该存在于数据库中");
        assertEquals(TEST_EMAIL, savedUser.getEmail());
        assertNotNull(savedUser.getPasswordHash(), "密码哈希不应为空");
        assertNotEquals(TEST_PASSWORD, savedUser.getPasswordHash(), "密码应以哈希形式存储");
    }

    @Test
    @DisplayName("用户登录 - 使用 username 登录，应返回 token")
    void shouldLoginWithUsernameSuccessfully() throws Exception {
        // 先注册
        registerTestUser();

        LoginRequest request = new LoginRequest();
        request.setAccount(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andReturn();

        // 反序列化验证 token 不为空
        String responseBody = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Result<AuthResponse> authResult = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructParametricType(Result.class,
                        objectMapper.getTypeFactory().constructType(AuthResponse.class)));
        assertNotNull(authResult.getData().getToken());
        assertTrue(authResult.getData().getToken().length() > 0);
    }

    @Test
    @DisplayName("用户登录 - 使用 email 登录，应返回 token")
    void shouldLoginWithEmailSuccessfully() throws Exception {
        // 先注册
        registerTestUser();

        LoginRequest request = new LoginRequest();
        request.setAccount(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
    }

    @Test
    @DisplayName("客户账号登录商家端 - 应拒绝签发跨端 Token")
    void shouldRejectCustomerLoginForMerchantClient() throws Exception {
        registerTestUser();

        LoginRequest request = new LoginRequest();
        request.setAccount(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);
        request.setClientType("MERCHANT_WEB");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("商品浏览接口 - 未登录用户可访问")
    void shouldAllowAnonymousProductBrowsing() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("错误密码登录 - 应返回错误")
    void shouldFailLoginWithWrongPassword() throws Exception {
        // 先注册
        registerTestUser();

        LoginRequest request = new LoginRequest();
        request.setAccount(TEST_USERNAME);
        request.setPassword("wrong_password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("用户名重复注册 - 应返回 400 错误")
    void shouldFailRegisterDuplicateUsername() throws Exception {
        // 先注册
        registerTestUser();

        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail("another_" + TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("username already exists"));
    }

    @Test
    @DisplayName("邮箱重复注册 - 应返回 400 错误")
    void shouldFailRegisterDuplicateEmail() throws Exception {
        // 先注册
        registerTestUser();

        RegisterRequest request = new RegisterRequest();
        request.setUsername("another_" + TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("email already exists"));
    }

    /**
     * 辅助方法：注册测试用户
     */
    private void registerTestUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setNickname("Test Auth User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}