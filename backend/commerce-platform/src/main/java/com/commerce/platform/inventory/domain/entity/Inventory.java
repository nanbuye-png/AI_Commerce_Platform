package com.commerce.platform.inventory.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 库存实体
 * <p>
 * Inventory Domain 的聚合根。
 * 采用三字段库存模型，满足约束：totalStock = availableStock + reservedStock
 * </p>
 *
 * <pre>
 * 字段职责：
 * - availableStock（可售库存）：当前可销售给客户的库存数量，顾客下单时以此为准
 * - reservedStock（已锁定库存）：已被订单预占但尚未支付的库存数量，订单创建时锁定
 * - totalStock（总库存）：仓库中该 SKU 的总实物库存，totalStock = availableStock + reservedStock
 * </pre>
 */
@Entity
@Table(name = "inventory", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sku_id", columnNames = "product_sku_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    /**
     * 关联商品 SKU ID（外键 → product_sku.id）
     */
    @Column(name = "product_sku_id", nullable = false, updatable = false)
    private Long productSkuId;

    /**
     * 可售库存
     * 当前可销售给客户的库存数量，顾客下单时以此为准（availableStock >= quantity 才可下单）
     */
    @Column(name = "available_stock", nullable = false)
    @Builder.Default
    private Integer availableStock = 0;

    /**
     * 已锁定库存
     * 已被订单预占但尚未支付的库存数量，订单创建时从 availableStock 转入 reservedStock
     */
    @Column(name = "reserved_stock", nullable = false)
    @Builder.Default
    private Integer reservedStock = 0;

    /**
     * 总库存
     * 仓库中该 SKU 的总实物库存，满足约束：totalStock = availableStock + reservedStock
     */
    @Column(name = "total_stock", nullable = false)
    @Builder.Default
    private Integer totalStock = 0;

    /**
     * 低库存阈值
     * 当 availableStock 低于此值时触发补货预警
     */
    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 0;

    /**
     * 乐观锁版本号
     * 用于高并发库存扣减场景，防止超卖
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}