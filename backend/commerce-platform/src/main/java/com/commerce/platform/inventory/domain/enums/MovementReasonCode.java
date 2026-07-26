package com.commerce.platform.inventory.domain.enums;

/**
 * 库存变动原因码
 * <p>
 * 用于标识库存变动的具体业务原因，形成完整审计链。
 * 预留未来业务能力，按需开放。
 * </p>
 */
public enum MovementReasonCode {

    /**
     * 正常入库（采购入库、调拨入库等）
     */
    NORMAL_INBOUND,

    /**
     * 手动调整（商家后台手动调整库存）
     */
    MANUAL_ADJUST,

    /**
     * 订单锁定（下单预占库存）
     */
    ORDER_RESERVE,

    /**
     * 订单释放（取消订单释放库存）
     */
    ORDER_RELEASE,

    /**
     * 订单扣减（支付成功扣减库存）
     */
    ORDER_DEDUCT,

    /**
     * 退货入库（售后退货回库存）
     */
    RETURN,

    /**
     * 报损出库（库存损坏报损）
     */
    DAMAGE,

    /**
     * 系统同步（数据迁移、ERP 同步等）
     */
    SYSTEM_SYNC
}