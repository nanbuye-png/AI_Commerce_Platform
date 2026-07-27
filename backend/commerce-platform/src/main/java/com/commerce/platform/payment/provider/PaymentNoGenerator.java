package com.commerce.platform.payment.provider;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 支付单号生成器
 * <p>
 * 格式：PAY + yyyyMMdd + 8位序列号
 * 例如：PAY2026072700000001
 * </p>
 */
@Component
public class PaymentNoGenerator {

    private static final String PREFIX = "PAY";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong counter = new AtomicLong(1);

    /**
     * 生成全局唯一的支付单号
     */
    public String generate() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        long seq = counter.getAndIncrement();
        return PREFIX + datePart + String.format("%08d", seq);
    }
}