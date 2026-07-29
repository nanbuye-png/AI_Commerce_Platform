package com.commerce.platform.inventory.stock.domain.exception;

/**
 * 库存释放异常
 * <p>
 * 当库存释放操作不合法时抛出，例如释放数量超过预占数量。
 * </p>
 */
public class InvalidStockReleaseException extends RuntimeException {

    private final Long productId;
    private final Long skuId;
    private final int reservedQuantity;
    private final int releaseQuantity;

    public InvalidStockReleaseException(Long productId, Long skuId, int reservedQuantity, int releaseQuantity) {
        super(String.format("库存释放不合法: productId=%d, skuId=%d, reserved=%d, release=%d",
                productId, skuId, reservedQuantity, releaseQuantity));
        this.productId = productId;
        this.skuId = skuId;
        this.reservedQuantity = reservedQuantity;
        this.releaseQuantity = releaseQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public int getReleaseQuantity() {
        return releaseQuantity;
    }
}