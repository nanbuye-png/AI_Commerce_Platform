package com.commerce.platform.user.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.user.dto.AuthResponse;
import com.commerce.platform.user.dto.LoginRequest;
import com.commerce.platform.user.dto.RegisterRequest;
import com.commerce.platform.user.dto.UserResponse;
import com.commerce.platform.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 * 提供注册和登录接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     * POST /api/auth/register
     *
     * @param request 注册请求
     * @return Result 包含注册成功的用户信息（不含密码哈希）
     */
    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     *
     * @param request 登录请求（account 支持 username 或 email）
     * @return Result 包含 AuthResponse（token + 用户信息）
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}