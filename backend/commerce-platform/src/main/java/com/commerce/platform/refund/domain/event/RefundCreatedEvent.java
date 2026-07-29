package com.commerce.platform.refund.domain.event;

import java.time.LocalDateTime;

/**
 * 退款创建事件
 */
public class RefundCreatedEvent {

    private final Long refundId;
    private final Long orderId;
    private final Long userId;
    private final LocalDateTime occurredAt;

    public RefundCreatedEvent(Long refundId, Long orderId, Long userId) {
        this.refundId = refundId;
        this.orderId = orderId;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getRefundId() {
        return refundId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}