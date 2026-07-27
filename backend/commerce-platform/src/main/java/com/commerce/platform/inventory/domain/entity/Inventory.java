package com.commerce.platform.inventory.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.exception.InsufficientInventoryException;
import com.commerce.platform.inventory.exception.InvalidInventoryStatusException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 库存实体
 * <p>
 * Inventory Domain 的聚合根。
 * 库存领域模型，承载库存生命周期状态转换与库存量变更行为。
 * 所有库存变更必须通过 Entity 提供的领域方法完成，禁止外部直接修改库存数量和状态。
 * </p>
 *
 * <pre>
 * 库存字段模型：
 * - availableStock（可售库存）：当前可销售的库存数量
 * - lockedStock（已锁定库存）：被订单占用但尚未扣减的库存数量
 * - soldStock（已售库存）：已正式扣减（出库）的库存数量
 * </pre>
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
public class Inventory extends BaseEntity {

    /**
     * 商品 ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * SKU ID（唯一）
     */
    @Column(name = "sku_id", nullable = false, unique = true)
    private Long skuId;

    /**
     * 可售库存
     */
    @Column(name = "available_stock", nullable = false)
    @Builder.Default
    private Integer availableStock = 0;

    /**
     * 已锁定库存（订单占用）
     */
    @Column(name = "locked_stock", nullable = false)
    @Builder.Default
    private Integer lockedStock = 0;

    /**
     * 已售库存（已扣减出库）
     */
    @Column(name = "sold_stock", nullable = false)
    @Builder.Default
    private Integer soldStock = 0;

    /**
     * 库存状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    /**
     * 创建时间
     */
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // ============================================
    // 领域行为 —— 库存状态转换与数量变更
    // 禁止外部直接调用 setAvailableStock() / setStatus()
    // ============================================

    /**
     * 锁定库存（冻结可用库存）
     * <p>
     * 状态：AVAILABLE → LOCKED（首次锁定）
     * LOCKED → LOCKED（追加锁定）
     * </p>
     *
     * @throws InsufficientInventoryException 当可用库存不足时抛出
     * @throws InvalidInventoryStatusException 当状态既不是 AVAILABLE 也不是 LOCKED 时抛出
     */
    public void lockStock() {
        if (this.status != InventoryStatus.AVAILABLE && this.status != InventoryStatus.LOCKED) {
            throw new InvalidInventoryStatusException(skuId, this.status.name(),
                    InventoryStatus.AVAILABLE.name() + " or " + InventoryStatus.LOCKED.name(), "lockStock");
        }

        if (availableStock <= 0) {
            throw new InsufficientInventoryException(skuId);
        }

        this.availableStock--;
        this.lockedStock++;
        this.status = InventoryStatus.LOCKED;
    }

    /**
     * 正式扣减库存（出库）
     * <p>
     * 状态：LOCKED → DEDUCTED
     * </p>
     *
     * @throws InvalidInventoryStatusException 当状态不是 LOCKED 时抛出
     */
    public void deductStock() {
        assertValidStatus(InventoryStatus.LOCKED, "deductStock");

        this.lockedStock--;
        this.soldStock++;
        this.status = InventoryStatus.DEDUCTED;
    }

    /**
     * 释放库存（取消锁定）
     * <p>
     * 状态：LOCKED → RELEASED（首次释放）
     * RELEASED → RELEASED（追加释放）
     * </p>
     *
     * @throws InvalidInventoryStatusException 当状态既不是 LOCKED 也不是 RELEASED 时抛出
     */
    public void releaseStock() {
        if (this.status != InventoryStatus.LOCKED && this.status != InventoryStatus.RELEASED) {
            throw new InvalidInventoryStatusException(skuId, this.status.name(),
                    InventoryStatus.LOCKED.name() + " or " + InventoryStatus.RELEASED.name(), "releaseStock");
        }

        this.lockedStock--;
        this.availableStock++;
        this.status = InventoryStatus.RELEASED;
    }

    /**
     * 恢复库存（如退款后恢复已售库存）
     * <p>
     * 状态：DEDUCTED → AVAILABLE
     * </p>
     *
     * @throws InvalidInventoryStatusException 当状态不是 DEDUCTED 时抛出
     */
    public void restoreStock() {
        assertValidStatus(InventoryStatus.DEDUCTED, "restoreStock");

        this.soldStock--;
        this.availableStock++;
        this.status = InventoryStatus.AVAILABLE;
    }

    // ============================================
    // 辅助方法
    // ============================================

    /**
     * 校验当前状态是否与期望状态一致
     */
    private void assertValidStatus(InventoryStatus expected, String operation) {
        if (this.status != expected) {
            throw new InvalidInventoryStatusException(skuId, this.status.name(), expected.name(), operation);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}