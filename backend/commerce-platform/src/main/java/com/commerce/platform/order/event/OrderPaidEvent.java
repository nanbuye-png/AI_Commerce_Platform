package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单支付事件
 */
@Getter
public class OrderPaidEvent {

    private final Long orderId;
    private final String orderNo;
    private final String paymentNo;
    private final LocalDateTime paidTime;

    public OrderPaidEvent(Long orderId, String orderNo, String paymentNo) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.paymentNo = paymentNo;
        this.paidTime = LocalDateTime.now();
    }
}