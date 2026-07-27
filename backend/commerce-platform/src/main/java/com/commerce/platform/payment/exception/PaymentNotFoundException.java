package com.commerce.platform.payment.exception;

/**
 * 支付记录不存在异常
 */
public class PaymentNotFoundException extends RuntimeException {

    private final int code;

    public PaymentNotFoundException(String paymentNo) {
        super(String.format("支付记录不存在：%s", paymentNo));
        this.code = 33004;
    }

    public int getCode() {
        return code;
    }
}