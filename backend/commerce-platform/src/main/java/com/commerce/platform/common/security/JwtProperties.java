package com.commerce.platform.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置属性
 * 从 application.yml 读取 jwt.secret 和 jwt.expiration
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 签名密钥（生产环境必须使用强密钥）
     */
    private String secret;

    /**
     * Token 过期时间（毫秒），默认 86400000（24小时）
     */
    private long expiration = 86400000L;

    private String customerWebSecret;
    private String merchantWebSecret;
    private String adminWebSecret;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
    public String getCustomerWebSecret() { return customerWebSecret; }
    public void setCustomerWebSecret(String customerWebSecret) { this.customerWebSecret = customerWebSecret; }
    public String getMerchantWebSecret() { return merchantWebSecret; }
    public void setMerchantWebSecret(String merchantWebSecret) { this.merchantWebSecret = merchantWebSecret; }
    public String getAdminWebSecret() { return adminWebSecret; }
    public void setAdminWebSecret(String adminWebSecret) { this.adminWebSecret = adminWebSecret; }
}