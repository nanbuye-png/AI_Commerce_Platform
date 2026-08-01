package com.commerce.platform.common.config;

import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.enums.UserRole;
import com.commerce.platform.user.enums.UserStatus;
import com.commerce.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始管理员账号初始化器
 *
 * 应用启动时，若数据库中不存在任何 ADMIN/SUPER_ADMIN 用户，
 * 则自动创建初始管理员账号（默认 admin/admin123，可通过 .env 覆盖）。
 * 密码仅在该账号首次创建时生效，已存在账号不会被修改。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin-username:admin}")
    private String adminUsername;

    @Value("${app.security.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.security.admin-email:admin@example.com}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Admin account initializer skipped: admin-username/admin-password not configured");
            return;
        }

        boolean adminExists = userRepository.findByUsername(adminUsername).isPresent()
                || userRepository.findByEmail(adminEmail).isPresent();
        if (adminExists) {
            return;
        }

        boolean anyAdmin = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.SUPER_ADMIN);
        if (anyAdmin) {
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .nickname("平台管理员")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(admin);
        log.info("Initial admin account created: username={}, email={}", adminUsername, adminEmail);
    }
}