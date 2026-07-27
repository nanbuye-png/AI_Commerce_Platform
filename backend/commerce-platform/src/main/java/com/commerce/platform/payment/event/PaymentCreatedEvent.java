package com.commerce.platform.payment.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 支付创建事件
 */
@Getter
public class PaymentCreatedEvent {

    private final String paymentNo;
    private final String orderNo;
    private final Long userId;
    private final LocalDateTime occurredAt;

    public PaymentCreatedEvent(String paymentNo, String orderNo, Long userId) {
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }
}