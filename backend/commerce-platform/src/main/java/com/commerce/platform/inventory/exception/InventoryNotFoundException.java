package com.commerce.platform.inventory.exception;

/**
 * 库存记录不存在异常
 * <p>
 * 当查询的库存记录在数据库中不存在时抛出。
 * </p>
 */
public class InventoryNotFoundException extends RuntimeException {

    private final int code = 34001;

    public InventoryNotFoundException(Long skuId) {
        super(String.format("库存记录不存在，SKU ID: %d", skuId));
    }

    public int getCode() {
        return code;
    }
}