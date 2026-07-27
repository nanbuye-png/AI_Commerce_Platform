package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单创建成功事件
 * <p>
 * 当订单创建成功时发布，由 CartCheckoutEventListener 监听 CartCheckedOutEvent 并创建订单成功后触发。
 * </p>
 */
@Getter
public class OrderCreatedSuccessEvent {

    private final String orderNo;
    private final String checkoutNo;
    private final Long userId;
    private final LocalDateTime occurredAt;

    public OrderCreatedSuccessEvent(String orderNo, String checkoutNo, Long userId) {
        this.orderNo = orderNo;
        this.checkoutNo = checkoutNo;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }
}