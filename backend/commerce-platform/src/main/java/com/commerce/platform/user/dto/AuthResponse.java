package com.commerce.platform.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 认证响应 DTO
 * 登录成功后返回 Token、用户信息及客户端类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /** JWT Token */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户角色（单角色兼容） */
    private String role;

    /** 角色列表 */
    private List<String> roles;

    /** 客户端类型 */
    private String clientType;
}