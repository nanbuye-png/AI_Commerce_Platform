package com.commerce.platform.profile.dto;

import lombok.Data;

/**
 * 用户个人资料 VO
 */
@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String phone;
}