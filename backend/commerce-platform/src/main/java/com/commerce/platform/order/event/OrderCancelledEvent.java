package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单取消事件
 */
@Getter
public class OrderCancelledEvent {

    private final String orderNo;
    private final Long buyerId;
    private final Long adminId;
    private final String cancelReason;
    private final List<OrderCreatedEvent.OrderItemDto> items;
    private final LocalDateTime occurredAt;

    public OrderCancelledEvent(String orderNo, Long buyerId, Long adminId, String cancelReason,
                                List<OrderCreatedEvent.OrderItemDto> items) {
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.adminId = adminId;
        this.cancelReason = cancelReason;
        this.items = items;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * 兼容旧调用（无 items）
     */
    public OrderCancelledEvent(String orderNo, Long buyerId, Long adminId, String cancelReason) {
        this(orderNo, buyerId, adminId, cancelReason, null);
    }
}