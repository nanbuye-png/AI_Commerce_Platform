package com.commerce.platform.inventory.reservation.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 预占库存命令
 * <p>
 * 接收来自事件或API的库存预占请求。
 * </p>
 */
@Getter
public class ReserveStockCommand {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private final Long orderId;

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private final Long productId;

    /** 预占数量 */
    @NotNull(message = "预占数量不能为空")
    private final Integer quantity;

    /**
     * 构造预占库存命令
     *
     * @param orderId   订单ID
     * @param productId 商品ID
     * @param quantity  预占数量
     */
    public ReserveStockCommand(Long orderId, Long productId, Integer quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }
}