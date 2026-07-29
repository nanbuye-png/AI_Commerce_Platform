package com.commerce.platform.fulfillment.infrastructure.persistence;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 履约单 JPA 实体
 * <p>
 * Infrastructure 层的 JPA 实体，用于持久化 Fulfillment 聚合。
 * Domain 层不依赖此类。
 * </p>
 */
@Entity
@Table(name = "fulfillment", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id", unique = true),
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_time", columnList = "created_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FulfillmentEntity extends BaseEntity {

    /** 订单ID（唯一） */
    @Column(name = "order_id", nullable = false, unique = true, updatable = false)
    private Long orderId;

    /** 商家ID */
    @Column(name = "merchant_id", nullable = false, updatable = false)
    private Long merchantId;

    /** 仓库ID（允许为空） */
    @Column(name = "warehouse_id")
    private Long warehouseId;

    /** 履约状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private FulfillmentStatus status = FulfillmentStatus.PENDING;

    /** 物流承运商 */
    @Column(name = "carrier", length = 100)
    private String carrier;

    /** 物流承运商编码 */
    @Column(name = "carrier_code", length = 50)
    private String carrierCode;

    /** 运单号 */
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    /** 收货地址 */
    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    /** 预计送达时间 */
    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

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