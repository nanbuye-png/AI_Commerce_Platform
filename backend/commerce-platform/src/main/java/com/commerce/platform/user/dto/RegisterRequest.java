package com.commerce.platform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求 DTO
 */
@Data
public class RegisterRequest {

    /**
     * 用户名（登录名）
     */
    @NotBlank(message = "username is required")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "password is required")
    private String password;

    /**
     * 昵称（可选）
     */
    private String nickname;

    /**
     * 手机号（可选）
     */
    private String phone;
}