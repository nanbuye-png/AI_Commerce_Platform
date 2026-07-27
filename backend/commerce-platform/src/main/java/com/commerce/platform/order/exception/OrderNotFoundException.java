package com.commerce.platform.order.exception;

/**
 * 订单不存在异常
 */
public class OrderNotFoundException extends RuntimeException {

    private final int code;

    public OrderNotFoundException(String orderNo) {
        super(String.format("订单不存在：%s", orderNo));
        this.code = 32004;
    }

    public int getCode() {
        return code;
    }
}