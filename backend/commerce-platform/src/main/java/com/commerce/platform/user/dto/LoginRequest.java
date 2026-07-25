package com.commerce.platform.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求 DTO
 * account 支持 username 或 email
 */
@Data
public class LoginRequest {

    /**
     * 账户（用户名或邮箱）
     */
    @NotBlank(message = "account is required")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "password is required")
    private String password;

    /**
     * 客户端类型 (CUSTOMER_WEB / MERCHANT_WEB / ADMIN_WEB)
     */
    private String clientType;
}