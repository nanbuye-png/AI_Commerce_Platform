package com.commerce.platform.shipping.infrastructure.persistence;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_record", indexes = {
    @Index(name = "idx_tr_shipment_id", columnList = "shipment_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingRecordEntity extends BaseEntity {

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

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