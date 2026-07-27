package com.commerce.platform.inventory.exception;

/**
 * 库存记录已存在异常
 * <p>
 * 当创建库存时 SKU ID 已存在时抛出。
 * </p>
 */
public class InventoryAlreadyExistsException extends RuntimeException {

    private final int code = 34005;

    public InventoryAlreadyExistsException(Long skuId) {
        super(String.format("库存记录已存在，SKU ID: %d", skuId));
    }

    public int getCode() {
        return code;
    }
}