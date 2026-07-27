package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单完成事件
 */
@Getter
public class OrderCompletedEvent {

    private final String orderNo;
    private final Long buyerId;
    private final LocalDateTime occurredAt;

    public OrderCompletedEvent(String orderNo, Long buyerId) {
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.occurredAt = LocalDateTime.now();
    }
}