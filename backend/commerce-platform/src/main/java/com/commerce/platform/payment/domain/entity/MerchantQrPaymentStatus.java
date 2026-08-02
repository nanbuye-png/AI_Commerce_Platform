package com.commerce.platform.payment.domain.entity;

/**
 * 商户二维码收款状态
 */
public enum MerchantQrPaymentStatus {
    /** 待支付（用户扫码后可支付） */
    WAITING,
    /** 已支付 */
    PAID,
    /** 已取消（用户主动取消） */
    CANCELLED,
    /** 已过期（15分钟未支付） */
    EXPIRED
}