package com.commerce.platform.returns.domain.valueobject;

/**
 * 退货原因 Value Object
 * <p>
 * 不可变，表示退货申请的原因类型。
 * </p>
 */
public enum ReturnReason {
    QUALITY_ISSUE,
    DAMAGED,
    WRONG_ITEM,
    MISSING_PART,
    NOT_AS_EXPECTED,
    OTHER
}