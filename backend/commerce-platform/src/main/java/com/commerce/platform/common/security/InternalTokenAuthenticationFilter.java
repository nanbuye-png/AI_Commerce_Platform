package com.commerce.platform.common.security;

import com.commerce.platform.ai.config.AiGatewayProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class InternalTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "X-Internal-Token";
    private static final AntPathRequestMatcher INTERNAL_AI_PATH =
            new AntPathRequestMatcher("/api/internal/ai/**");

    private final AiGatewayProperties properties;

    public InternalTokenAuthenticationFilter(AiGatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !INTERNAL_AI_PATH.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String configuredToken = properties.internalToken();
        String providedToken = request.getHeader(TOKEN_HEADER);
        if (tokensMatch(configuredToken, providedToken)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "ai-service",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private boolean tokensMatch(String configuredToken, String providedToken) {
        if (!StringUtils.hasText(configuredToken) || !StringUtils.hasText(providedToken)) {
            return false;
        }
        return MessageDigest.isEqual(
                configuredToken.getBytes(StandardCharsets.UTF_8),
                providedToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}