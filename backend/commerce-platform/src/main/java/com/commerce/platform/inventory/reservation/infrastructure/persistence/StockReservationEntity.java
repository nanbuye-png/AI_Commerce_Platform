package com.commerce.platform.inventory.reservation.infrastructure.persistence;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 库存预占 JPA 实体
 * <p>
 * Infrastructure 层的 JPA 实体，用于持久化 StockReservation 聚合。
 * Domain 层不依赖此类。
 * </p>
 */
@Entity
@Table(name = "stock_reservation", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservationEntity extends BaseEntity {

    /** 订单ID */
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    /** 商品ID */
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    /** 预占数量 */
    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    /** 预占状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.RESERVED;

    /** 创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 确认时间 */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /** 释放时间 */
    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @PrePersist
    protected void onCreate() {
        setCreatedTime(LocalDateTime.now());
        setUpdatedTime(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedTime(LocalDateTime.now());
    }
}