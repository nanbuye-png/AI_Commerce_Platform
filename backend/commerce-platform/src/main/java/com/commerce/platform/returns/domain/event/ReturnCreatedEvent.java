package com.commerce.platform.returns.domain.event;

import java.time.LocalDateTime;

public class ReturnCreatedEvent {
    private final Long returnId;
    private final Long orderId;
    private final Long userId;
    private final LocalDateTime occurredAt;

    public ReturnCreatedEvent(Long returnId, Long orderId, Long userId) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getReturnId() { return returnId; }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}