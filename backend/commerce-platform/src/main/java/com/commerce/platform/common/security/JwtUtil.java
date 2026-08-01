package com.commerce.platform.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * JWT 工具类
 * 支持多端 Token 签发与验证
 */
@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public enum ClientType {
        CUSTOMER_WEB, MERCHANT_WEB, ADMIN_WEB
    }

    private static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    private static final String ROLE_MERCHANT = "ROLE_MERCHANT";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private SecretKey getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String getSecretForClient(ClientType clientType) {
        return switch (clientType) {
            case CUSTOMER_WEB -> jwtProperties.getCustomerWebSecret();
            case MERCHANT_WEB -> jwtProperties.getMerchantWebSecret();
            case ADMIN_WEB -> jwtProperties.getAdminWebSecret();
        };
    }

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 角色列表
     * @param clientType 客户端类型
     * @return JWT token string
     */
    public String generateToken(Long userId, String username, List<String> roles, ClientType clientType) {
        validateRolesForClient(clientType, roles);
        String secret = getSecretForClient(clientType);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .claim("clientType", clientType.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSigningKey(secret))
                .compact();
    }

    /**
     * 解析 Token Claims
     */
    public Claims parseToken(String token, ClientType clientType) {
        String secret = getSecretForClient(clientType);
        Claims claims = parseTokenWithSecret(token, secret);
        validateClaimsForClient(claims, clientType);
        return claims;
    }

    /**
     * 通用解析（自动匹配）
     */
    public Claims parseToken(String token) {
        JwtException lastException = null;
        for (ClientType clientType : ClientType.values()) {
            try {
                return parseToken(token, clientType);
            } catch (JwtException e) {
                lastException = e;
            }
        }
        throw lastException != null ? lastException : new JwtException("Unable to parse token");
    }

    private Claims parseTokenWithSecret(String token, String secret) {
        if (secret == null || secret.isBlank()) {
            throw new JwtException("JWT secret is not configured");
        }
        return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    private void validateClaimsForClient(Claims claims, ClientType verifiedClientType) {
        String claimedClientType = claims.get("clientType", String.class);
        if (!verifiedClientType.name().equals(claimedClientType)) {
            throw new JwtException("JWT client type does not match signing key");
        }
        validateRolesForClient(verifiedClientType, claims.get("roles", List.class));
    }

    private void validateRolesForClient(ClientType clientType, List<String> roles) {
        Set<String> allowedRoles = switch (clientType) {
            case CUSTOMER_WEB -> Set.of(ROLE_CUSTOMER);
            case MERCHANT_WEB -> Set.of(ROLE_MERCHANT);
            case ADMIN_WEB -> Set.of(ROLE_ADMIN);
        };
        if (roles == null || roles.isEmpty() || !allowedRoles.containsAll(roles)) {
            throw new JwtException("JWT roles are not allowed for client " + clientType);
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 从 Token 中提取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * 从 Token 中提取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).get("username", String.class);
    }

    /**
     * 从 Token 中提取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return parseToken(token).get("roles", List.class);
    }

    /**
     * 从 Token 中提取客户端类型
     */
    public String getClientTypeFromToken(String token) {
        return parseToken(token).get("clientType", String.class);
    }
}