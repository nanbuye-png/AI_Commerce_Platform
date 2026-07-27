package com.commerce.platform.inventory.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存释放事件
 * <p>
 * 当库存从 LOCKED 状态转换为 RELEASED 状态时发布。
 * </p>
 */
@Getter
public class InventoryReleasedEvent {

    private final Long inventoryId;
    private final Long skuId;
    private final Integer quantity;
    private final String orderNo;
    private final LocalDateTime releaseTime;

    public InventoryReleasedEvent(Long inventoryId, Long skuId, Integer quantity, String orderNo) {
        this.inventoryId = inventoryId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.orderNo = orderNo;
        this.releaseTime = LocalDateTime.now();
    }
}
