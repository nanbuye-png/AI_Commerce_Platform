package com.commerce.platform.inventory.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.inventory.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 库存预占实体
 * <p>
 * 记录订单对库存的锁定信息。
 * 一条 Reservation 对应一个订单中的一个 SKU 锁定记录。
 * 设计为独立表，不依赖 inventory 中的单个字段记录所有锁定关系。
 * </p>
 *
 * <pre>
 * 生命周期：
 * 创建订单 → RESERVE → [ACTIVE]
 *   ├── 支付成功 → DEDUCT → [DEDUCTED]
 *   ├── 取消订单 → RELEASE → [RELEASED]
 *   ├── 超时未支付 → 定时任务 → [EXPIRED]
 *   └── 售后部分退款 → 部分 RELEASE → [RELEASED]
 * </pre>
 */
@Entity
@Table(name = "inventory_reservation", indexes = {
    @Index(name = "idx_reservation_inventory_id", columnList = "inventory_id"),
    @Index(name = "idx_reservation_sku_id", columnList = "product_sku_id"),
    @Index(name = "idx_reservation_order_id", columnList = "order_id"),
    @Index(name = "idx_reservation_status_expired", columnList = "status, expire_time"),
    @Index(name = "idx_reservation_created_time", columnList = "created_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservation extends BaseEntity {

    /**
     * 预占编号（全局唯一，业务可读）
     */
    @Column(name = "reservation_no", nullable = false, unique = true, length = 64, updatable = false)
    private String reservationNo;

    /**
     * 关联库存记录 ID（外键 → inventory.id）
     */
    @Column(name = "inventory_id", nullable = false, updatable = false)
    private Long inventoryId;

    /**
     * 关联商品 SKU ID（外键 → product_sku.id）
     */
    @Column(name = "product_sku_id", nullable = false, updatable = false)
    private Long productSkuId;

    /**
     * 订单 ID（业务关联，无外键约束，避免与 Order Domain 耦合）
     */
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    /**
     * 锁定数量
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 预占状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.ACTIVE;

    /**
     * 过期时间
     * 超时未支付时定时任务将状态置为 EXPIRED 并自动释放库存
     */
    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;
}