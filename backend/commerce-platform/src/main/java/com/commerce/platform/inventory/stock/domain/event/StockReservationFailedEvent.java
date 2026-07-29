package com.commerce.platform.inventory.stock.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存预占失败事件
 * <p>
 * 当库存不足导致预占失败时发布。
 * </p>
 */
@Getter
public class StockReservationFailedEvent {

    /** 订单ID */
    private final Long orderId;

    /** 商品ID */
    private final Long productId;

    /** 请求数量 */
    private final Integer requestedQuantity;

    /** 可用数量 */
    private final Integer availableQuantity;

    /** 失败原因 */
    private final String reason;

    /** 事件发生时间 */
    private final LocalDateTime occurredOn;

    /**
     * 构造库存预占失败事件
     *
     * @param orderId          订单ID
     * @param productId        商品ID
     * @param requestedQuantity 请求数量
     * @param availableQuantity 可用数量
     */
    public StockReservationFailedEvent(Long orderId, Long productId,
                                        Integer requestedQuantity, Integer availableQuantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.reason = "库存不足: requested=" + requestedQuantity + ", available=" + availableQuantity;
        this.occurredOn = LocalDateTime.now();
    }
}