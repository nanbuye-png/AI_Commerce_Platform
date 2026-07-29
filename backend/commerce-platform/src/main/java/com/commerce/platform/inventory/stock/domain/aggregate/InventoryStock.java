package com.commerce.platform.inventory.stock.domain.aggregate;

import com.commerce.platform.inventory.stock.domain.exception.InsufficientStockException;

/**
 * 库存聚合根（库存预占模型）
 * <p>
 * 表示商品库存的领域模型，承载库存预占、释放、确认等行为。
 * 库存模型：availableQuantity + reservedQuantity = totalQuantity
 * 所有数量变更必须通过领域方法完成，禁止外部直接修改字段。
 * </p>
 */
public class InventoryStock {

    private Long id;
    private Long productId;
    private Long skuId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;

    /**
     * 创建库存
     *
     * @param productId         商品ID
     * @param skuId             SKU ID
     * @param availableQuantity 初始可售数量
     * @return 新建的库存
     */
    public static InventoryStock create(Long productId, Long skuId, Integer availableQuantity) {
        InventoryStock stock = new InventoryStock();
        stock.productId = productId;
        stock.skuId = skuId;
        stock.availableQuantity = availableQuantity;
        stock.reservedQuantity = 0;
        stock.soldQuantity = 0;
        return stock;
    }

    /**
     * 从持久化恢复库存（全字段构造）
     */
    public static InventoryStock restore(Long id, Long productId, Long skuId,
                                         Integer availableQuantity, Integer reservedQuantity,
                                         Integer soldQuantity) {
        InventoryStock stock = new InventoryStock();
        stock.id = id;
        stock.productId = productId;
        stock.skuId = skuId;
        stock.availableQuantity = availableQuantity;
        stock.reservedQuantity = reservedQuantity;
        stock.soldQuantity = soldQuantity;
        return stock;
    }

    // ============================================
    // 领域行为
    // ============================================

    /**
     * 预占库存
     * <p>
     * 可用库存 >= 预占数量，否则抛 InsufficientStockException。
     * availableQuantity -= quantity
     * reservedQuantity += quantity
     * </p>
     *
     * @param quantity 预占数量
     * @throws InsufficientStockException 库存不足
     */
    public void reserve(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("预占数量必须大于0: " + quantity);
        }
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(productId, skuId, quantity, availableQuantity);
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    /**
     * 释放库存（订单取消）
     * <p>
     * reservedQuantity -= quantity
     * availableQuantity += quantity
     * </p>
     *
     * @param quantity 释放数量
     */
    public void release(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("释放数量必须大于0: " + quantity);
        }
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("预占库存不足，无法释放: reserved=" + reservedQuantity + ", release=" + quantity);
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    /**
     * 确认库存（支付成功）
     * <p>
     * reservedQuantity -= quantity
     * soldQuantity += quantity
     * </p>
     *
     * @param quantity 确认数量
     */
    public void confirm(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("确认数量必须大于0: " + quantity);
        }
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("预占库存不足，无法确认: reserved=" + reservedQuantity + ", confirm=" + quantity);
        }
        this.reservedQuantity -= quantity;
        this.soldQuantity += quantity;
    }

    /**
     * 设置聚合根ID（仅用于持久化后的赋值）
     */
    public void setId(Long id) {
        this.id = id;
    }

    // ============================================
    // Getters
    // ============================================

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public Integer getSoldQuantity() {
        return soldQuantity;
    }

    public Integer getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }
}