package com.commerce.platform.returns.domain.valueobject;

/**
 * 退货状态枚举
 * <p>
 * 正常流程：
 * REQUESTED → APPROVED → RETURNING → RECEIVED → COMPLETED
 * <p>
 * 异常流程：
 * REQUESTED → REJECTED
 * RETURNING → FAILED
 * </p>
 */
public enum ReturnStatus {
    REQUESTED,
    APPROVED,
    RETURNING,
    RECEIVED,
    COMPLETED,
    REJECTED,
    FAILED;

    /**
     * 检查当前状态是否可以迁移到目标状态
     *
     * @param target 目标状态
     * @return true 如果迁移合法
     */
    public boolean canTransitionTo(ReturnStatus target) {
        return switch (this) {
            case REQUESTED -> target == APPROVED || target == REJECTED;
            case APPROVED -> target == RETURNING;
            case RETURNING -> target == RECEIVED || target == FAILED;
            case RECEIVED -> target == COMPLETED;
            case COMPLETED, REJECTED, FAILED -> false;
        };
    }
}