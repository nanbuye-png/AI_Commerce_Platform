package com.commerce.platform.inventory.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 库存锁定成功事件
 * <p>
 * 当前通过 ApplicationEventPublisher 发布（同步应用内事件），
 * 后续 Sprint 扩展为 MQ 消息。
 * </p>
 */
@Data
@AllArgsConstructor
public class InventoryReservedEvent {

    private String reservationNo;
    private Long inventoryId;
    private Long productSkuId;
    private Long orderId;
    private Integer quantity;
}