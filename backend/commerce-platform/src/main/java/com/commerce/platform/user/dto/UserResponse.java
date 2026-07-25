package com.commerce.platform.user.dto;

import com.commerce.platform.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应 DTO
 * 不包含密码哈希字段，用于注册和用户查询场景的安全返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从 User 实体构建 UserResponse（不含 passwordHash）
     *
     * @param user 用户实体
     * @return UserResponse
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .build();
    }
}