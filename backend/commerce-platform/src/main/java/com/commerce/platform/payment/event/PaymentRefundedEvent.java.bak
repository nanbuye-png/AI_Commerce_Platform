package com.commerce.platform.payment.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 退款成功事件
 */
@Getter
public class PaymentRefundedEvent {

    private final String paymentNo;
    private final String orderNo;
    private final LocalDateTime occurredAt;

    public PaymentRefundedEvent(String paymentNo, String orderNo) {
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.occurredAt = LocalDateTime.now();
    }
}