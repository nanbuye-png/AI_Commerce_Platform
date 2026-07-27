package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单发货事件
 */
@Getter
public class OrderShippedEvent {

    private final String orderNo;
    private final Long merchantId;
    private final LocalDateTime occurredAt;

    public OrderShippedEvent(String orderNo, Long merchantId) {
        this.orderNo = orderNo;
        this.merchantId = merchantId;
        this.occurredAt = LocalDateTime.now();
    }
}