package com.commerce.platform.inventory.exception;

/**
 * 重复的库存预占记录异常
 */
public class DuplicateInventoryReservationException extends RuntimeException {
    private final int code = 34007;

    public DuplicateInventoryReservationException(String orderNo, Long skuId) {
        super(String.format("重复的库存预占记录，订单号: %s, SKU ID: %d", orderNo, skuId));
    }

    public int getCode() { return code; }
}