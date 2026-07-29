package com.commerce.platform.refund.domain.valueobject;

/**
 * 退款原因 Value Object
 * <p>
 * 不可变，表示退款申请的原因类型。
 * </p>
 */
public enum RefundReason {
    QUALITY_ISSUE,
    WRONG_PRODUCT,
    CUSTOMER_CHANGE_MIND,
    DAMAGED,
    OTHER
}