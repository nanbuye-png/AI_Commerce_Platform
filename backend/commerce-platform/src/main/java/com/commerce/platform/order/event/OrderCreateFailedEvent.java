package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单创建失败事件
 * <p>
 * 当订单创建失败时发布，由 CartCheckoutEventListener 监听 CartCheckedOutEvent 并创建订单失败后触发。
 * Cart 端监听此事件以执行补偿操作（恢复 CartItem 状态）。
 * </p>
 */
@Getter
public class OrderCreateFailedEvent {

    private final String checkoutNo;
    private final String reason;
    private final LocalDateTime occurredAt;

    public OrderCreateFailedEvent(String checkoutNo, String reason) {
        this.checkoutNo = checkoutNo;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
    }
}