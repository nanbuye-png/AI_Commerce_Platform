package com.commerce.platform.inventory.exception;

/**
 * 非法库存状态转换异常
 * <p>
 * 当库存状态转换不符合预定义的状态机规则时抛出。
 * 例如：AVAILABLE 状态直接调用 deductStock() 等。
 * </p>
 */
public class InvalidInventoryStatusException extends RuntimeException {

    private final int code = 34003;

    public InvalidInventoryStatusException(Long skuId, String currentStatus, String expectedStatus, String operation) {
        super(String.format("库存状态转换非法，SKU ID: %d，当前状态: %s，期望状态: %s，操作: %s",
                skuId, currentStatus, expectedStatus, operation));
    }

    public int getCode() {
        return code;
    }
}