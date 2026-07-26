package com.commerce.platform.inventory.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.inventory.domain.enums.MovementReasonCode;
import com.commerce.platform.inventory.domain.enums.MovementSourceType;
import com.commerce.platform.inventory.domain.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 库存流水实体
 * <p>
 * 记录每次库存变动的审计日志，采用 Append-Only 模式：
 * - 仅 INSERT，不 UPDATE，不 DELETE（物理或逻辑）
 * - 永久保存，禁止修改历史记录
 * </p>
 *
 * <pre>
 * 流水记录包含变化前后的三字段快照，可还原任意时刻的库存状态。
 * 当订单、支付、库存三方对账不一致时，此表是唯一的仲裁依据。
 * </pre>
 */
@Entity
@Table(name = "inventory_movement", indexes = {
    @Index(name = "idx_movement_sku_id", columnList = "product_sku_id"),
    @Index(name = "idx_movement_inventory_id", columnList = "inventory_id"),
    @Index(name = "idx_movement_type", columnList = "movement_type"),
    @Index(name = "idx_movement_source_type", columnList = "source_type"),
    @Index(name = "idx_movement_reason_code", columnList = "reason_code"),
    @Index(name = "idx_movement_source_id", columnList = "source_id"),
    @Index(name = "idx_movement_business_id", columnList = "business_id"),
    @Index(name = "idx_movement_sku_created", columnList = "product_sku_id, created_time"),
    @Index(name = "idx_movement_created_time", columnList = "created_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement extends BaseEntity {

    /**
     * 流水编号（全局唯一，业务可读）
     */
    @Column(name = "movement_no", nullable = false, unique = true, length = 64, updatable = false)
    private String movementNo;

    /**
     * 关联商品 SKU ID（外键 → product_sku.id）
     */
    @Column(name = "product_sku_id", nullable = false, updatable = false)
    private Long productSkuId;

    /**
     * 关联库存记录 ID（外键 → inventory.id）
     */
    @Column(name = "inventory_id", nullable = false, updatable = false)
    private Long inventoryId;

    /**
     * 变动类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20, updatable = false)
    private MovementType movementType;

    /**
     * 来源类型（MERCHANT / ORDER / ADMIN / SYSTEM）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20, updatable = false)
    private MovementSourceType sourceType;

    /**
     * 来源 ID（订单号、入库单号等）
     */
    @Column(name = "source_id", length = 64, updatable = false)
    private String sourceId;

    /**
     * 原因码（NORMAL_INBOUND / ORDER_RESERVE 等）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 30, updatable = false)
    private MovementReasonCode reasonCode;

    /**
     * 变动数量（正数 = 增加，负数 = 减少）
     */
    @Column(nullable = false, updatable = false)
    private Integer quantity;

    /**
     * 变动前可售库存快照
     */
    @Column(name = "before_available", nullable = false, updatable = false)
    private Integer beforeAvailable;

    /**
     * 变动后可售库存快照
     */
    @Column(name = "after_available", nullable = false, updatable = false)
    private Integer afterAvailable;

    /**
     * 变动前已锁定库存快照
     */
    @Column(name = "before_reserved", nullable = false, updatable = false)
    @Builder.Default
    private Integer beforeReserved = 0;

    /**
     * 变动后已锁定库存快照
     */
    @Column(name = "after_reserved", nullable = false, updatable = false)
    @Builder.Default
    private Integer afterReserved = 0;

    /**
     * 操作人 ID（商家/管理员/系统）
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 操作人名称
     */
    @Column(name = "operator_name", length = 64)
    private String operatorName;

    /**
     * 业务单号（订单号/入库单号等，方便追溯）
     */
    @Column(name = "business_id", length = 64, updatable = false)
    private String businessId;

    /**
     * 备注
     */
    @Column(length = 256, updatable = false)
    private String remark;
}