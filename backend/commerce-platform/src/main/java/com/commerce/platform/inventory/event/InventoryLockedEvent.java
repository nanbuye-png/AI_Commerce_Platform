package com.commerce.platform.inventory.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存锁定事件
 * <p>
 * 当库存从 AVAILABLE 状态转换为 LOCKED 状态时发布。
 * </p>
 */
@Getter
public class InventoryLockedEvent {

    private final Long inventoryId;
    private final Long productId;
    private final Long skuId;
    private final Integer quantity;
    private final String orderNo;
    private final LocalDateTime lockTime;

    public InventoryLockedEvent(Long inventoryId, Long productId, Long skuId, Integer quantity, String orderNo) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.orderNo = orderNo;
        this.lockTime = LocalDateTime.now();
    }
}
