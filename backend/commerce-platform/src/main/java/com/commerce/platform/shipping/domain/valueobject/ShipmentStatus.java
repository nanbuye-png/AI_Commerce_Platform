package com.commerce.platform.shipping.domain.valueobject;

/**
 * 配送单状态枚举
 * <p>
 * 定义配送单的完整生命周期状态。
 * </p>
 *
 * <pre>
 * CREATED → READY_TO_SHIP → SHIPPED → IN_TRANSIT → DELIVERED
 * 任意非终态 → CANCELLED / FAILED
 * </pre>
 */
public enum ShipmentStatus {

    CREATED,
    READY_TO_SHIP,
    SHIPPED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED,
    FAILED;

    public boolean canTransitionTo(ShipmentStatus target) {
        return switch (this) {
            case CREATED -> target == READY_TO_SHIP || target == CANCELLED || target == FAILED;
            case READY_TO_SHIP -> target == SHIPPED || target == CANCELLED || target == FAILED;
            case SHIPPED -> target == IN_TRANSIT || target == FAILED;
            case IN_TRANSIT -> target == DELIVERED || target == FAILED;
            case DELIVERED, CANCELLED, FAILED -> false;
        };
    }
}