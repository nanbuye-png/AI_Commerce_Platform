package com.commerce.platform.refund.domain.event;

import java.time.LocalDateTime;

/**
 * 退款完成事件
 */
public class RefundCompletedEvent {

    private final Long refundId;
    private final Long orderId;
    private final LocalDateTime completedAt;

    public RefundCompletedEvent(Long refundId, Long orderId) {
        this.refundId = refundId;
        this.orderId = orderId;
        this.completedAt = LocalDateTime.now();
    }

    public Long getRefundId() {
        return refundId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}