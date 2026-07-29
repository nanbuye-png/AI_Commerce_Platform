package com.commerce.platform.payment.domain.event;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentCreatedEvent {
    private final Long paymentId;
    private final Long orderId;
    private final BigDecimal amount;
    private final LocalDateTime occurredOn;

    public PaymentCreatedEvent(Long paymentId, Long orderId, BigDecimal amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.occurredOn = LocalDateTime.now();
    }
}