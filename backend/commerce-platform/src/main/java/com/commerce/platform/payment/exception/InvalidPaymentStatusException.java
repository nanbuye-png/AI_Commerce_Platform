package com.commerce.platform.payment.exception;

/**
 * 非法支付状态转换异常
 * 当支付状态不允许执行特定操作时抛出
 */
public class InvalidPaymentStatusException extends RuntimeException {

    private final int code;

    public InvalidPaymentStatusException(String paymentNo, String currentStatus, String operation) {
        super(String.format("支付状态不允许%s：paymentNo=%s, currentStatus=%s", operation, paymentNo, currentStatus));
        this.code = 33005;
    }

    public int getCode() {
        return code;
    }
}