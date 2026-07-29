package com.commerce.platform.inventory.stock.infrastructure.persistence;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 库存 JPA 实体（预占模型）
 * <p>
 * Infrastructure 层的 JPA 实体，用于持久化 InventoryStock 聚合。
 * Domain 层不依赖此类。
 * 库存模型：available_quantity + reserved_quantity = total
 * </p>
 */
@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_sku_id", columnList = "sku_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockEntity extends BaseEntity {

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** SKU ID */
    @Column(name = "sku_id", nullable = false, unique = true)
    private Long skuId;

    /** 可售库存 */
    @Column(name = "available_stock", nullable = false)
    @Builder.Default
    private Integer availableStock = 0;

    /** 已锁定/预占库存 */
    @Column(name = "reserved_stock", nullable = false)
    @Builder.Default
    private Integer reservedStock = 0;

    /** 已售库存 */
    @Column(name = "sold_stock", nullable = false)
    @Builder.Default
    private Integer soldStock = 0;
}