package com.commerce.platform.fulfillment.domain.valueobject;

/**
 * 履约单状态枚举
 * <p>
 * 定义履约单的完整生命周期状态。
 * 状态迁移必须遵守合法路径，非法跳转会抛出异常。
 * </p>
 *
 * <pre>
 * 合法状态迁移路径：
 * PENDING
 *   ↓
 * PROCESSING
 *   ↓
 * PICKING
 *   ↓
 * PACKING
 *   ↓
 * WAITING_SHIPMENT
 *   ↓
 * SHIPPED
 *   ↓
 * DELIVERED
 *   ↓
 * COMPLETED
 *
 * 任意状态 → CANCELLED（非终态可取消）
 * 任意状态 → FAILED（异常中断）
 * </pre>
 */
public enum FulfillmentStatus {

    /** 待处理 */
    PENDING,
    /** 处理中 */
    PROCESSING,
    /** 拣货中 */
    PICKING,
    /** 打包中 */
    PACKING,
    /** 待发货 */
    WAITING_SHIPMENT,
    /** 已发货 */
    SHIPPED,
    /** 运输中/已送达但未确认 */
    DELIVERED,
    /** 已完成 */
    COMPLETED,
    /** 已失败 */
    FAILED,
    /** 已取消 */
    CANCELLED;

    /**
     * 判断从当前状态是否可以迁移到目标状态
     *
     * @param target 目标状态
     * @return true 如果迁移合法
     */
    public boolean canTransitionTo(FulfillmentStatus target) {
        return switch (this) {
            case PENDING -> target == PROCESSING || target == CANCELLED || target == FAILED;
            case PROCESSING -> target == PICKING || target == CANCELLED || target == FAILED;
            case PICKING -> target == PACKING || target == CANCELLED || target == FAILED;
            case PACKING -> target == WAITING_SHIPMENT || target == CANCELLED || target == FAILED;
            case WAITING_SHIPMENT -> target == SHIPPED || target == CANCELLED || target == FAILED;
            case SHIPPED -> target == DELIVERED || target == FAILED;
            case DELIVERED -> target == COMPLETED || target == FAILED;
            case COMPLETED -> false;
            case FAILED -> false;
            case CANCELLED -> false;
        };
    }
}