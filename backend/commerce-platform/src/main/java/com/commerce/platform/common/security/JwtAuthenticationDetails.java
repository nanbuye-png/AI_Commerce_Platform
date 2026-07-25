package com.commerce.platform.common.security;

public class JwtAuthenticationDetails {
    private final Long userId;
    private final String username;
    private final String clientType;

    public JwtAuthenticationDetails(Long userId, String username, String clientType) {
        this.userId = userId;
        this.username = username;
        this.clientType = clientType;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getClientType() { return clientType; }
}