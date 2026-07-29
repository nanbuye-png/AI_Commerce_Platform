package com.commerce.platform.warehouse.infrastructure.persistence;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.warehouse.domain.valueobject.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "picking_task", indexes = {
    @Index(name = "idx_pt_fulfillment_id", columnList = "fulfillment_id"),
    @Index(name = "idx_pt_warehouse_id", columnList = "warehouse_id"),
    @Index(name = "idx_pt_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickingTaskEntity extends BaseEntity {

    @Column(name = "fulfillment_id", nullable = false)
    private Long fulfillmentId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.CREATED;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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