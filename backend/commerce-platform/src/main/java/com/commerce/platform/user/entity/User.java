package com.commerce.platform.user.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.user.enums.UserRole;
import com.commerce.platform.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * 用户实体
 * 映射数据表: users
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /**
     * 用户名（登录名），唯一且非空
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 邮箱，唯一且非空
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 密码哈希值，非空
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * 昵称
     */
    @Column(length = 100)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatar;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 用户角色，默认 CUSTOMER
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    /**
     * 用户状态，默认 ACTIVE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}