package com.commerce.platform.payment.exception;

/**
 * 支付创建失败异常
 */
public class PaymentCreateFailedException extends RuntimeException {

    private final int code;

    public PaymentCreateFailedException(String orderNo, String reason) {
        super(String.format("支付创建失败：orderNo=%s, reason=%s", orderNo, reason));
        this.code = 33007;
    }

    public int getCode() {
        return code;
    }
}