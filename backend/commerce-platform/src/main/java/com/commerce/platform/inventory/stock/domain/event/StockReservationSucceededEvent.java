package com.commerce.platform.inventory.stock.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存预占成功事件
 * <p>
 * 当库存预占成功（Inventory 预占 + StockReservation 创建）后发布。
 * </p>
 */
@Getter
public class StockReservationSucceededEvent {

    /** 预占ID */
    private final Long reservationId;

    /** 订单ID */
    private final Long orderId;

    /** 商品ID */
    private final Long productId;

    /** 预占数量 */
    private final Integer quantity;

    /** 事件发生时间 */
    private final LocalDateTime occurredOn;

    /**
     * 构造库存预占成功事件
     *
     * @param reservationId 预占ID
     * @param orderId       订单ID
     * @param productId     商品ID
     * @param quantity      预占数量
     */
    public StockReservationSucceededEvent(Long reservationId, Long orderId, Long productId, Integer quantity) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.occurredOn = LocalDateTime.now();
    }
}