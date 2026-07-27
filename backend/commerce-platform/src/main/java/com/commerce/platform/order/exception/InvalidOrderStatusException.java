package com.commerce.platform.order.exception;

/**
 * 无效的订单状态异常
 * 当订单状态不允许执行特定操作时抛出
 */
public class InvalidOrderStatusException extends RuntimeException {

    private final int code;

    public InvalidOrderStatusException(String message) {
        super(message);
        this.code = 32005;
    }

    public InvalidOrderStatusException(String orderNo, String currentStatus, String operation) {
        super(String.format("订单状态不允许%s：orderNo=%s, currentStatus=%s", operation, orderNo, currentStatus));
        this.code = 32005;
    }

    public int getCode() {
        return code;
    }
}