package com.commerce.platform.payment.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 支付失败事件
 */
@Getter
public class PaymentFailedEvent {

    private final String paymentNo;
    private final String orderNo;
    private final String failReason;
    private final LocalDateTime occurredAt;

    public PaymentFailedEvent(String paymentNo, String orderNo, String failReason) {
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.failReason = failReason;
        this.occurredAt = LocalDateTime.now();
    }
}