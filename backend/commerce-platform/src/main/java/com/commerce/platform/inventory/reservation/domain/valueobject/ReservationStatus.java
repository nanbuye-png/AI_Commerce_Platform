package com.commerce.platform.inventory.reservation.domain.valueobject;

/**
 * 库存预占状态枚举
 * <p>
 * 定义库存预占的完整生命周期状态。
 * 状态迁移必须遵守合法路径，非法跳转会抛出异常。
 * </p>
 *
 * <pre>
 * 合法状态迁移路径：
 * RESERVED
 *   ↓
 * CONFIRMED
 *
 * RESERVED
 *   ↓
 * RELEASED
 *
 * RESERVED
 *   ↓
 * FAILED
 * </pre>
 */
public enum ReservationStatus {

    /** 已预占（初始状态） */
    RESERVED,
    /** 已确认（支付成功，库存正式占用） */
    CONFIRMED,
    /** 已释放（订单取消，库存释放） */
    RELEASED,
    /** 已失败 */
    FAILED;

    /**
     * 判断从当前状态是否可以迁移到目标状态
     *
     * @param target 目标状态
     * @return true 如果迁移合法
     */
    public boolean canTransitionTo(ReservationStatus target) {
        return switch (this) {
            case RESERVED -> target == CONFIRMED || target == RELEASED || target == FAILED;
            case CONFIRMED -> false;
            case RELEASED -> false;
            case FAILED -> false;
        };
    }
}