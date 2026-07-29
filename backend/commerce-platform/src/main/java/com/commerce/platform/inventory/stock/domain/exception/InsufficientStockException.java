package com.commerce.platform.inventory.stock.domain.exception;

/**
 * 库存不足异常
 * <p>
 * 当可用库存不足以执行预占操作时抛出。
 * </p>
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final Long skuId;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long productId, Long skuId, int requested, int available) {
        super(String.format("库存不足: productId=%d, skuId=%d, requested=%d, available=%d",
                productId, skuId, requested, available));
        this.productId = productId;
        this.skuId = skuId;
        this.requested = requested;
        this.available = available;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}