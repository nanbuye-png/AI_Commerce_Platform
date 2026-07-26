package com.commerce.platform.order.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单编号生成器
 * <p>
 * 格式：yyyyMMddHHmmss + 6 位随机数
 * 示例：20260726103045123456
 * 长度固定为 20 位。
 * 后续可替换为 Snowflake 等分布式 ID 方案。
 * </p>
 */
@Component
public class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int RANDOM_BOUND = 1000000;

    /**
     * 生成订单编号
     *
     * @return 全局唯一订单编号
     */
    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(RANDOM_BOUND);
        return timestamp + String.format("%06d", random);
    }
}