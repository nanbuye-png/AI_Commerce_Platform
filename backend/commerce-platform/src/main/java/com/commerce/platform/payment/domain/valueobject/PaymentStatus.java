package com.commerce.platform.payment.domain.valueobject;

/**
 * 支付状态枚举
 * <p>
 * 定义支付交易的完整生命周期状态。
 * 状态迁移必须遵守合法路径，非法跳转会抛出异常。
 * </p>
 *
 * <pre>
 * 合法状态迁移路径：
 * CREATED
 *   ↓
 * PROCESSING
 *   ↓
 * PAID
 *
 * CREATED  → CANCELLED
 * CREATED  → FAILED
 * PROCESSING → FAILED
 * </pre>
 */
public enum PaymentStatus {

    /** 已创建 */
    CREATED,
    /** 处理中 */
    PROCESSING,
    /** 已支付成功 */
    PAID,
    /** 已取消 */
    CANCELLED,
    /** 已失败 */
    FAILED;

    /**
     * 判断从当前状态是否可以迁移到目标状态
     *
     * @param target 目标状态
     * @return true 如果迁移合法
     */
    public boolean canTransitionTo(PaymentStatus target) {
        return switch (this) {
            case CREATED -> target == PROCESSING || target == CANCELLED || target == FAILED;
            case PROCESSING -> target == PAID || target == FAILED;
            case PAID -> false;
            case CANCELLED -> false;
            case FAILED -> false;
        };
    }
}