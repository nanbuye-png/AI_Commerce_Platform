package com.commerce.platform.payment.exception;

/**
 * 重复处理支付异常
 * 当支付已经被处理过（SUCCESS/FAILED）时再次操作抛出
 */
public class PaymentAlreadyProcessedException extends RuntimeException {

    private final int code;

    public PaymentAlreadyProcessedException(String paymentNo) {
        super(String.format("支付已处理，不可重复操作：paymentNo=%s", paymentNo));
        this.code = 33006;
    }

    public int getCode() {
        return code;
    }
}