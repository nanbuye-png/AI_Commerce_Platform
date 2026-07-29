package com.commerce.platform.inventory.reservation.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存释放事件
 * <p>
 * 当库存预占被释放后发布，事件保持不可变。
 * </p>
 */
@Getter
public class StockReleasedEvent {

    /** 预占ID */
    private final Long reservationId;

    /** 订单ID */
    private final Long orderId;

    /** 商品ID */
    private final Long productId;

    /** 释放数量 */
    private final Integer quantity;

    /** 事件发生时间 */
    private final LocalDateTime occurredOn;

    /**
     * 构造库存释放事件
     *
     * @param reservationId 预占ID
     * @param orderId       订单ID
     * @param productId     商品ID
     * @param quantity      释放数量
     */
    public StockReleasedEvent(Long reservationId, Long orderId, Long productId, Integer quantity) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.occurredOn = LocalDateTime.now();
    }
}