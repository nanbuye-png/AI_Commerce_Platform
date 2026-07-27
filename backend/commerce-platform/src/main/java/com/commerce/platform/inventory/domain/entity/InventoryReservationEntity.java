package com.commerce.platform.inventory.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.inventory.domain.enums.InvReservationStatus;
import com.commerce.platform.inventory.exception.InvalidInventoryStatusException;
import jakarta.persistence.*;
import lombok.*;

/**
 * 库存预占记录实体
 * <p>
 * 记录订单占用的库存信息，用于防止重复释放库存。
 * 状态变化必须通过领域方法，禁止外部直接 setStatus()。
 * </p>
 *
 * <pre>
 * 状态转换：
 * LOCKED ──release()──→ RELEASED
 * LOCKED ──deduct()──→ DEDUCTED
 * </pre>
 */
@Entity
@Table(name = "inventory_reservation", uniqueConstraints = {
    @UniqueConstraint(name = "uk_order_sku", columnNames = {"order_no", "sku_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationEntity extends BaseEntity {

    @Column(name = "order_no", nullable = false, length = 32, updatable = false)
    private String orderNo;

    @Column(name = "sku_id", nullable = false, updatable = false)
    private Long skuId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvReservationStatus status = InvReservationStatus.LOCKED;

    // ============================================
    // 领域行为
    // ============================================

    /**
     * 释放预占
     * LOCKED → RELEASED
     */
    public void release() {
        assertValidStatus(InvReservationStatus.LOCKED, "release");
        this.status = InvReservationStatus.RELEASED;
    }

    /**
     * 扣减预占
     * LOCKED → DEDUCTED
     */
    public void deduct() {
        assertValidStatus(InvReservationStatus.LOCKED, "deduct");
        this.status = InvReservationStatus.DEDUCTED;
    }

    /**
     * 校验当前状态
     */
    private void assertValidStatus(InvReservationStatus expected, String operation) {
        if (this.status != expected) {
            throw new InvalidInventoryStatusException(this.skuId, this.status.name(), expected.name(), operation);
        }
    }
}