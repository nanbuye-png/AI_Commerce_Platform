package com.commerce.platform.inventory.domain.enums;

/**
 * 库存预占状态枚举
 */
public enum ReservationStatus {

    /**
     * 锁定中 — 预占生效，库存已被锁定
     */
    ACTIVE,

    /**
     * 已扣减 — 订单支付成功，库存已从预占转为实际扣减
     */
    DEDUCTED,

    /**
     * 已释放 — 订单取消/支付失败/售后释放，库存已恢复
     */
    RELEASED,

    /**
     * 已过期 — 超时未支付自动释放
     */
    EXPIRED
}