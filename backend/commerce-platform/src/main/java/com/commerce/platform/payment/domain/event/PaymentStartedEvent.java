package com.commerce.platform.payment.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PaymentStartedEvent {
    private final Long paymentId;
    private final Long orderId;
    private final LocalDateTime occurredOn;

    public PaymentStartedEvent(Long paymentId, Long orderId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.occurredOn = LocalDateTime.now();
    }
}