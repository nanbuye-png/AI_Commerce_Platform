package com.commerce.platform.inventory.exception;

/**
 * 库存不足异常
 * <p>
 * 当可用库存不足以执行锁定/扣减操作时抛出。
 * </p>
 */
public class InsufficientInventoryException extends RuntimeException {

    private final int code = 34002;

    public InsufficientInventoryException(Long skuId) {
        super(String.format("库存不足，SKU ID: %d", skuId));
    }

    public int getCode() {
        return code;
    }
}