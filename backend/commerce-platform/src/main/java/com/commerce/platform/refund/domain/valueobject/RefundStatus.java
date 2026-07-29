package com.commerce.platform.refund.domain.valueobject;

/**
 * 退款状态枚举
 * <p>
 * 正常流程：
 * REQUESTED → APPROVED → PROCESSING → COMPLETED
 * <p>
 * 异常流程：
 * REQUESTED → REJECTED
 * PROCESSING → FAILED
 * </p>
 */
public enum RefundStatus {
    REQUESTED,
    APPROVED,
    PROCESSING,
    COMPLETED,
    REJECTED,
    FAILED;

    /**
     * 检查当前状态是否可以迁移到目标状态
     *
     * @param target 目标状态
     * @return true 如果迁移合法
     */
    public boolean canTransitionTo(RefundStatus target) {
        return switch (this) {
            case REQUESTED -> target == APPROVED || target == REJECTED;
            case APPROVED -> target == PROCESSING;
            case PROCESSING -> target == COMPLETED || target == FAILED;
            case COMPLETED, REJECTED, FAILED -> false;
        };
    }
}