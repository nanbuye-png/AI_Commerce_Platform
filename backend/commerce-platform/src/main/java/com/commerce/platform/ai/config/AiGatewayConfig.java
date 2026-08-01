package com.commerce.platform.ai.config;

import com.commerce.platform.common.security.InternalTokenAuthenticationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(AiGatewayProperties.class)
public class AiGatewayConfig {

    @Bean
    public InternalTokenAuthenticationFilter internalTokenAuthenticationFilter(
            AiGatewayProperties properties) {
        return new InternalTokenAuthenticationFilter(properties);
    }

    @Bean
    public HttpClient aiServiceHttpClient(AiGatewayProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
    }
}