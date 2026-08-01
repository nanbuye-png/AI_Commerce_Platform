package com.commerce.platform.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.ai")
public record AiGatewayProperties(
        URI baseUrl,
        String internalToken,
        Duration connectTimeout,
        Duration requestTimeout
) {
}