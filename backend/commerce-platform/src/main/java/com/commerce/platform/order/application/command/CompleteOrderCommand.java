package com.commerce.platform.order.application.command;

/**
 * 订单完成请求命令
 * <p>
 * 表示订单完成的请求。
 * </p>
 */
public class CompleteOrderCommand {

    private final Long orderId;

    public CompleteOrderCommand(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}