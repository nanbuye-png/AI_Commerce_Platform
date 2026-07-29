package com.commerce.platform.shipping.domain.aggregate;

import com.commerce.platform.shipping.domain.exception.InvalidShipmentStatusException;
import com.commerce.platform.shipping.domain.valueobject.DeliveryStatus;
import com.commerce.platform.shipping.domain.valueobject.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * 配送单聚合根
 * <p>
 * 表示一次配送任务，负责维护配送生命周期状态。
 * 状态变更必须通过领域方法完成。
 * </p>
 */
public class Shipment {

    private Long id;
    private Long fulfillmentId;
    private Long packingTaskId;
    private String carrier;
    private String trackingNumber;
    private ShipmentStatus status;
    private DeliveryStatus deliveryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    public static Shipment create(Long fulfillmentId, Long packingTaskId, String carrier) {
        Shipment s = new Shipment();
        s.fulfillmentId = fulfillmentId;
        s.packingTaskId = packingTaskId;
        s.carrier = carrier;
        s.status = ShipmentStatus.CREATED;
        s.deliveryStatus = DeliveryStatus.WAITING;
        s.createdAt = LocalDateTime.now();
        return s;
    }

    public static Shipment restore(Long id, Long fulfillmentId, Long packingTaskId,
                                   String carrier, String trackingNumber,
                                   ShipmentStatus status, DeliveryStatus deliveryStatus,
                                   LocalDateTime createdAt, LocalDateTime shippedAt,
                                   LocalDateTime deliveredAt) {
        Shipment s = new Shipment();
        s.id = id;
        s.fulfillmentId = fulfillmentId;
        s.packingTaskId = packingTaskId;
        s.carrier = carrier;
        s.trackingNumber = trackingNumber;
        s.status = status;
        s.deliveryStatus = deliveryStatus;
        s.createdAt = createdAt;
        s.shippedAt = shippedAt;
        s.deliveredAt = deliveredAt;
        return s;
    }

    // ========== 领域行为 ==========

    /** CREATED → READY_TO_SHIP */
    public void markReadyToShip() {
        transitionTo(ShipmentStatus.READY_TO_SHIP, "markReadyToShip");
    }

    /** READY_TO_SHIP → SHIPPED */
    public void ship(String trackingNumber) {
        transitionTo(ShipmentStatus.SHIPPED, "ship");
        this.trackingNumber = trackingNumber;
        this.shippedAt = LocalDateTime.now();
    }

    /** SHIPPED → IN_TRANSIT */
    public void markInTransit() {
        transitionTo(ShipmentStatus.IN_TRANSIT, "markInTransit");
    }

    /** IN_TRANSIT → DELIVERED */
    public void markDelivered() {
        transitionTo(ShipmentStatus.DELIVERED, "markDelivered");
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /** 取消配送 */
    public void cancel() {
        transitionTo(ShipmentStatus.CANCELLED, "cancel");
    }

    /** 配送失败 */
    public void fail() {
        transitionTo(ShipmentStatus.FAILED, "fail");
        this.deliveryStatus = DeliveryStatus.FAILED;
    }

    /** 设置配送状态（末端维度） */
    public void updateDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    private void transitionTo(ShipmentStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidShipmentStatusException(this.id, this.status.name(), target.name());
        }
        this.status = target;
    }

    public void setId(Long id) { this.id = id; }

    // ========== Getters ==========
    public Long getId() { return id; }
    public Long getFulfillmentId() { return fulfillmentId; }
    public Long getPackingTaskId() { return packingTaskId; }
    public String getCarrier() { return carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public ShipmentStatus getStatus() { return status; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
}