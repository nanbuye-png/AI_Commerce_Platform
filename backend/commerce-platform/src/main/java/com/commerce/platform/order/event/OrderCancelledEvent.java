package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单取消事件
 * <p>
 * Sprint 20 Step 3B: 新增 orderId 字段，统一事件契约。
 * </p>
 */
@Getter
public class OrderCancelledEvent {

    private final Long orderId;
    private final String orderNo;
    private final Long buyerId;
    private final Long adminId;
    private final String cancelReason;
    private final List<OrderCreatedEvent.OrderItemDto> items;
    private final LocalDateTime occurredAt;

    public OrderCancelledEvent(Long orderId, String orderNo, Long buyerId, Long adminId, String cancelReason,
                                List<OrderCreatedEvent.OrderItemDto> items) {
        this.orderId = orderId;
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
    public OrderCancelledEvent(Long orderId, String orderNo, Long buyerId, Long adminId, String cancelReason) {
        this(orderId, orderNo, buyerId, adminId, cancelReason, null);
    }
}