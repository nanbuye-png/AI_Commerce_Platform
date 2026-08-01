package com.commerce.platform.ai.controller;

import com.commerce.platform.ai.dto.ChatStreamRequest;
import com.commerce.platform.ai.service.AiGatewayService;
import com.commerce.platform.common.config.SecurityConfig;
import com.commerce.platform.common.security.JwtProperties;
import com.commerce.platform.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantAiController.class)
@Import({SecurityConfig.class, JwtProperties.class, JwtUtil.class})
@TestPropertySource(properties = {
        "jwt.customer-web-secret=test-customer-web-secret-with-minimum-32-bytes",
        "jwt.merchant-web-secret=test-merchant-web-secret-with-minimum-32-bytes",
        "jwt.admin-web-secret=test-admin-web-secret-with-minimum-32-bytes",
        "jwt.expiration=3600000"
})
class MerchantAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private AiGatewayService aiGatewayService;

    @Test
    void streamsAiEventsForMerchant() throws Exception {
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = invocation.getArgument(1);
            consumer.accept("event: message\ndata: {\"type\":\"token\",\"content\":\"好\"}\n\n");
            consumer.accept("event: done\ndata: {\"conversation_id\":\"conv_1\",\"message_id\":\"msg_1\"}\n\n");
            return null;
        }).when(aiGatewayService).stream(any(ChatStreamRequest.class), any());

        mockMvc.perform(post("/api/merchant/ai/chat/stream")
                        .header("Authorization", "Bearer " + token("ROLE_MERCHANT", JwtUtil.ClientType.MERCHANT_WEB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"优化商品标题\",\"conversation_id\":\"conv_1\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(
                        "event: message\ndata: {\"type\":\"token\",\"content\":\"好\"}\n\n"
                                + "event: done\ndata: {\"conversation_id\":\"conv_1\",\"message_id\":\"msg_1\"}\n\n"));
    }

    @Test
    void rejectsCustomerToken() throws Exception {
        mockMvc.perform(post("/api/merchant/ai/chat/stream")
                        .header("Authorization", "Bearer " + token("ROLE_CUSTOMER", JwtUtil.ClientType.CUSTOMER_WEB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isForbidden());

        verify(aiGatewayService, never()).stream(any(), any());
    }

    @Test
    void validatesBlankMessageBeforeCallingAiService() throws Exception {
        mockMvc.perform(post("/api/merchant/ai/chat/stream")
                        .header("Authorization", "Bearer " + token("ROLE_MERCHANT", JwtUtil.ClientType.MERCHANT_WEB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(aiGatewayService, never()).stream(any(), any());
    }

    private String token(String role, JwtUtil.ClientType clientType) {
        return jwtUtil.generateToken(1L, "tester", java.util.List.of(role), clientType);
    }
}