package com.commerce.platform.inventory.domain.enums;

/**
 * 库存变动来源类型
 * <p>
 * 标识库存变动的触发方，用于审计溯源。
 * </p>
 */
public enum MovementSourceType {

    /**
     * 商家操作（入库、调整等）
     */
    MERCHANT,

    /**
     * 订单流程（锁定、释放、扣减等）
     */
    ORDER,

    /**
     * 平台管理员操作
     */
    ADMIN,

    /**
     * 系统自动操作（定时任务、数据同步等）
     */
    SYSTEM
}