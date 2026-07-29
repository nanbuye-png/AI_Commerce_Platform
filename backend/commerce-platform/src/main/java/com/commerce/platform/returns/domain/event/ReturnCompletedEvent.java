package com.commerce.platform.returns.domain.event;

import java.time.LocalDateTime;

public class ReturnCompletedEvent {
    private final Long returnId;
    private final Long orderId;
    private final LocalDateTime occurredAt;

    public ReturnCompletedEvent(Long returnId, Long orderId) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getReturnId() { return returnId; }
    public Long getOrderId() { return orderId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}