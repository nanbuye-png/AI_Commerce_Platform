package com.commerce.platform.inventory.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存扣减事件
 * <p>
 * 当库存从 LOCKED 状态转换为 DEDUCTED 状态时发布。
 * </p>
 */
@Getter
public class InventoryDeductedEvent {

    private final Long inventoryId;
    private final String orderNo;
    private final Long skuId;
    private final Integer quantity;
    private final LocalDateTime deductTime;

    public InventoryDeductedEvent(Long inventoryId, String orderNo, Long skuId, Integer quantity) {
        this.inventoryId = inventoryId;
        this.orderNo = orderNo;
        this.skuId = skuId;
        this.quantity = quantity;
        this.deductTime = LocalDateTime.now();
    }
}