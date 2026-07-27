package com.commerce.platform.payment.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付成功事件
 */
@Getter
public class PaymentSuccessEvent {

    private final String paymentNo;
    private final String orderNo;
    private final String transactionNo;
    private final BigDecimal amount;
    private final LocalDateTime successTime;

    public PaymentSuccessEvent(String paymentNo, String orderNo, String transactionNo, BigDecimal amount) {
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.transactionNo = transactionNo;
        this.amount = amount;
        this.successTime = LocalDateTime.now();
    }
}