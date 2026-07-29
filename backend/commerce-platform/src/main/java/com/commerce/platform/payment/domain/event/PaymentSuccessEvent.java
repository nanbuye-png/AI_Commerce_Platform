package com.commerce.platform.payment.domain.event;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentSuccessEvent {
    private final Long paymentId;
    private final Long orderId;
    private final String transactionNo;
    private final BigDecimal amount;
    private final LocalDateTime occurredOn;

    public PaymentSuccessEvent(Long paymentId, Long orderId, String transactionNo, BigDecimal amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.transactionNo = transactionNo;
        this.amount = amount;
        this.occurredOn = LocalDateTime.now();
    }
}