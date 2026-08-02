package com.commerce.platform.user.repository;

import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.enums.UserRole;
import com.commerce.platform.user.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository 集成测试
 * 验证 User Entity 可正常持久化到数据库
 */
@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("保存用户并验证可写入数据库")
    void shouldSaveAndRetrieveUser() {
        // 准备测试用户
        User user = User.builder()
                .username("test_user_" + System.currentTimeMillis())
                .email("test_" + System.currentTimeMillis() + "@aicommerce.com")
                .passwordHash("hashed_password_test")
                .nickname("Test User")
                .phone("13800138000")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        // 保存
        User saved = userRepository.save(user);
        assertNotNull(saved.getId(), "保存后应自动生成 ID");

        // 通过 ID 查询
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent(), "应能通过 ID 查到用户");
        assertEquals("Test User", found.get().getNickname());
        assertEquals(UserRole.CUSTOMER, found.get().getRole());
        assertEquals(UserStatus.ACTIVE, found.get().getStatus());

        // 通过 username 查询
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        assertTrue(byUsername.isPresent(), "应能通过 username 查到用户");

        // 通过 email 查询
        Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
        assertTrue(byEmail.isPresent(), "应能通过 email 查到用户");
    }
}