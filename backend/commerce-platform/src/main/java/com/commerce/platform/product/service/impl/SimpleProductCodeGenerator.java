package com.commerce.platform.product.service.impl;

import com.commerce.platform.product.service.ProductCodeGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简易商品编码生成器（临时实现）
 * 格式: PROD + yyyyMMddHHmmss + 6位序列号
 * 后续可替换为雪花ID或数据库序列生成器
 */
@Component
public class SimpleProductCodeGenerator implements ProductCodeGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String PREFIX = "PROD";

    @Override
    public String generateProductCode() {
        long seq = COUNTER.incrementAndGet() % 1000000;
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return PREFIX + timestamp + String.format("%06d", seq);
    }
}