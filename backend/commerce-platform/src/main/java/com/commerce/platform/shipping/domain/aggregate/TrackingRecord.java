package com.commerce.platform.shipping.domain.aggregate;

import java.time.LocalDateTime;

/**
 * 物流轨迹记录聚合根
 * <p>
 * 记录物流运输过程中的轨迹信息。
 * TrackingRecord 不修改 Shipment 状态，通过 Event 通信。
 * </p>
 */
public class TrackingRecord {

    private Long id;
    private Long shipmentId;
    private String location;
    private String description;
    private String status;
    private LocalDateTime occurredAt;

    public static TrackingRecord create(Long shipmentId, String location, String description, String status) {
        TrackingRecord record = new TrackingRecord();
        record.shipmentId = shipmentId;
        record.location = location;
        record.description = description;
        record.status = status;
        record.occurredAt = LocalDateTime.now();
        return record;
    }

    public static TrackingRecord restore(Long id, Long shipmentId, String location,
                                         String description, String status, LocalDateTime occurredAt) {
        TrackingRecord record = new TrackingRecord();
        record.id = id;
        record.shipmentId = shipmentId;
        record.location = location;
        record.description = description;
        record.status = status;
        record.occurredAt = occurredAt;
        return record;
    }

    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }
    public Long getShipmentId() { return shipmentId; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}