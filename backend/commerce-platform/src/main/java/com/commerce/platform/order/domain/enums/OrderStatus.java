package com.commerce.platform.order.domain.enums;

/**
 * 订单主状态枚举
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PROCESSING,
    SHIPPED,
    COMPLETED,
    CANCELLED,
    REFUNDING,
    REFUNDED,
    CLOSED
}