package com.commerce.platform.inventory.exception;

/**
 * 库存预占记录不存在异常
 */
public class InventoryReservationNotFoundException extends RuntimeException {
    private final int code = 34006;

    public InventoryReservationNotFoundException(String orderNo, Long skuId) {
        super(String.format("库存预占记录不存在，订单号: %s, SKU ID: %d", orderNo, skuId));
    }

    public int getCode() { return code; }
}