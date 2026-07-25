package com.commerce.platform.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 公共配置
 * 预留 JPA 审计功能，后续迭代启用
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.commerce.platform")
@EnableJpaAuditing
public class JpaConfig {
}