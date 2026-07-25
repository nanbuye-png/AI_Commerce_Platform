package com.commerce.platform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试
 * 验证 Token 生成、解析、验证、多端 ClientType 能力
 */
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void shouldGenerateTokenSuccessfully() {
        List<String> roles = List.of("ROLE_CUSTOMER");
        String token = jwtUtil.generateToken(1L, "testuser", roles, JwtUtil.ClientType.CUSTOMER_WEB);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void shouldParseTokenAndExtractClaims() {
        List<String> roles = List.of("ROLE_MERCHANT");
        String token = jwtUtil.generateToken(2L, "alice", roles, JwtUtil.ClientType.MERCHANT_WEB);

        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals("2", claims.getSubject());
        assertEquals("alice", claims.get("username", String.class));
        assertEquals(List.of("ROLE_MERCHANT"), claims.get("roles", List.class));
        assertEquals("MERCHANT_WEB", claims.get("clientType", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void shouldGenerateTokensWithDifferentClientTypes() {
        List<String> customerRoles = List.of("ROLE_CUSTOMER");
        List<String> adminRoles = List.of("ROLE_ADMIN");

        String customerToken = jwtUtil.generateToken(1L, "customer", customerRoles, JwtUtil.ClientType.CUSTOMER_WEB);
        String merchantToken = jwtUtil.generateToken(2L, "merchant", List.of("ROLE_MERCHANT"), JwtUtil.ClientType.MERCHANT_WEB);
        String adminToken = jwtUtil.generateToken(3L, "admin", adminRoles, JwtUtil.ClientType.ADMIN_WEB);

        assertEquals("CUSTOMER_WEB", jwtUtil.getClientTypeFromToken(customerToken));
        assertEquals("MERCHANT_WEB", jwtUtil.getClientTypeFromToken(merchantToken));
        assertEquals("ADMIN_WEB", jwtUtil.getClientTypeFromToken(adminToken));
    }

    @Test
    void shouldValidateToken() {
        List<String> roles = List.of("ROLE_ADMIN");
        String token = jwtUtil.generateToken(3L, "bob", roles, JwtUtil.ClientType.ADMIN_WEB);

        assertTrue(jwtUtil.validateToken(token));

        String tamperedToken = token + "tampered";
        assertFalse(jwtUtil.validateToken(tamperedToken));
    }

    @Test
    void shouldExtractUserIdAndRoles() {
        List<String> roles = List.of("ROLE_CUSTOMER", "ROLE_MERCHANT");
        String token = jwtUtil.generateToken(5L, "multi", roles, JwtUtil.ClientType.CUSTOMER_WEB);

        assertEquals(5L, jwtUtil.getUserIdFromToken(token));
        assertEquals("multi", jwtUtil.getUsernameFromToken(token));
        assertEquals(roles, jwtUtil.getRolesFromToken(token));
        assertEquals("CUSTOMER_WEB", jwtUtil.getClientTypeFromToken(token));
    }
}
