package com.commerce.platform.user.service;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.common.security.JwtUtil;
import com.commerce.platform.user.dto.AuthResponse;
import com.commerce.platform.user.dto.LoginRequest;
import com.commerce.platform.user.dto.RegisterRequest;
import com.commerce.platform.user.dto.UserResponse;
import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.enums.UserRole;
import com.commerce.platform.user.enums.UserStatus;
import com.commerce.platform.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务层
 * 处理注册、登录、用户查询等核心业务逻辑
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册
     */
    @Transactional
    public Result<UserResponse> register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return Result.error(400, "username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return Result.error(400, "email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        UserResponse userResponse = UserResponse.from(savedUser);
        return Result.success(userResponse);
    }

    /**
     * 用户登录
     */
    public Result<AuthResponse> login(LoginRequest request) {
        String account = request.getAccount();
        Optional<User> userOptional = userRepository.findByUsername(account);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(account);
        }
        if (userOptional.isEmpty()) {
            return Result.error(400, "account or password is incorrect");
        }

        User user = userOptional.get();
        if (user.getStatus() != UserStatus.ACTIVE) {
            return Result.error(400, "account is disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return Result.error(400, "account or password is incorrect");
        }

        // Determine client type from request (default CUSTOMER_WEB)
        JwtUtil.ClientType clientType = JwtUtil.ClientType.CUSTOMER_WEB;
        if (request.getClientType() != null) {
            try {
                clientType = JwtUtil.ClientType.valueOf(request.getClientType());
            } catch (IllegalArgumentException ignored) {}
        }

        List<String> roles = List.of("ROLE_" + user.getRole().name());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles, clientType);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .roles(roles)
                .clientType(clientType.name())
                .build();

        return Result.success(authResponse);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}