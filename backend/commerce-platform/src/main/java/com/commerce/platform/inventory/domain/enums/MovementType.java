package com.commerce.platform.inventory.domain.enums;

/**
 * 库存变动类型枚举
 * <p>
 * 完整预留所有已知业务类型，后续 Sprint 按需开放业务能力，不修改模型。
 * </p>
 */
public enum MovementType {

    /**
     * 入库 — 采购/调拨入库，增加可售库存
     */
    INBOUND,

    /**
     * 出库 — 发货/调拨出库，减少总库存
     */
    OUTBOUND,

    /**
     * 锁定 — 订单创建预占库存，减少可售库存，增加锁定库存
     */
    RESERVE,

    /**
     * 释放 — 取消订单释放库存，减少锁定库存，增加可售库存
     */
    RELEASE,

    /**
     * 扣减 — 支付成功扣减库存，减少锁定库存和总库存
     */
    DEDUCT,

    /**
     * 调整 — 商家/管理员手动调整库存
     */
    ADJUST,

    /**
     * 退货入库 — 售后退货（预留，后续 Sprint 启用）
     */
    RETURN,

    /**
     * 报损 — 库存损坏报损出库（预留，后续 Sprint 启用）
     */
    DAMAGE
}