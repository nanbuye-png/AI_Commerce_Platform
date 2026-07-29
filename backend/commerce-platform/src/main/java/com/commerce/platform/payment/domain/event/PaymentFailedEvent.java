package com.commerce.platform.payment.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PaymentFailedEvent {
    private final Long paymentId;
    private final Long orderId;
    private final String reason;
    private final LocalDateTime occurredOn;

    public PaymentFailedEvent(Long paymentId, Long orderId, String reason) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.reason = reason;
        this.occurredOn = LocalDateTime.now();
    }
}