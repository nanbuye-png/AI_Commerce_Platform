package com.commerce.platform.common.controller;

import com.commerce.platform.common.entity.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试认证接口
 * 用于前后端联调验证 JWT 认证是否正常工作
 * 测试完成后建议删除此 Controller
 */
@RestController
@RequestMapping("/api/test")
public class TestAuthController {

    /**
     * GET /api/test/auth
     * 需要携带 JWT Token 才能访问
     * 返回当前登录用户信息
     */
    @GetMapping("/auth")
    public Result<Map<String, Object>> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "Not authenticated");
        }

        String username = authentication.getName();
        Object userId = authentication.getDetails();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("UNKNOWN");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        userInfo.put("role", role);

        return Result.success(userInfo);
    }
}