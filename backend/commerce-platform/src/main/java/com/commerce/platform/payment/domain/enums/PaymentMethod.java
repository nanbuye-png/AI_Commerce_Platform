package com.commerce.platform.payment.domain.enums;

/**
 * 支付方式枚举
 * <p>
 * WECHAT_PAY — 微信支付（预留）
 * ALI_PAY — 支付宝支付（预留）
 * MOCK — 模拟支付（开发/测试用）
 * </p>
 */
public enum PaymentMethod {
    WECHAT_PAY,
    ALI_PAY,
    MOCK
}