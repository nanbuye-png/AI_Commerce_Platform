package com.commerce.platform.payment.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二维码支付完成事件
 */
@Getter
public class PaymentCompletedEvent {
    private final Long paymentId;
    private final Long orderId;
    private final String orderNo;
    private final BigDecimal amount;
    private final LocalDateTime occurredOn;

    public PaymentCompletedEvent(Long paymentId, Long orderId, String orderNo, BigDecimal amount, LocalDateTime occurredOn) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.amount = amount;
        this.occurredOn = occurredOn;
    }
}
