package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单关闭事件
 */
@Getter
public class OrderClosedEvent {

    private final String orderNo;
    private final Long adminId;
    private final String closeReason;
    private final LocalDateTime occurredAt;

    public OrderClosedEvent(String orderNo, Long adminId, String closeReason) {
        this.orderNo = orderNo;
        this.adminId = adminId;
        this.closeReason = closeReason;
        this.occurredAt = LocalDateTime.now();
    }
}