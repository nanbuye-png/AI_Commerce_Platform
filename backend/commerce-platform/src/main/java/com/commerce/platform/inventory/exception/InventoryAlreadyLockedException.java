package com.commerce.platform.inventory.exception;

/**
 * 重复锁库存异常
 * <p>
 * 当已锁定的库存再次被尝试锁定时抛出。
 * </p>
 */
public class InventoryAlreadyLockedException extends RuntimeException {

    private final int code = 34004;

    public InventoryAlreadyLockedException(Long skuId) {
        super(String.format("库存已被锁定，SKU ID: %d", skuId));
    }

    public int getCode() {
        return code;
    }
}