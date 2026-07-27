package com.commerce.platform.payment.exception;

/**
 * 支付金额不匹配异常
 * 当 Provider 返回金额与订单支付金额不一致时抛出
 */
public class PaymentAmountMismatchException extends RuntimeException {

    private final int code;

    public PaymentAmountMismatchException(String paymentNo, java.math.BigDecimal expected, java.math.BigDecimal actual) {
        super(String.format("支付金额不匹配：paymentNo=%s, expected=%s, actual=%s", paymentNo, expected, actual));
        this.code = 33007;
    }

    public int getCode() {
        return code;
    }
}