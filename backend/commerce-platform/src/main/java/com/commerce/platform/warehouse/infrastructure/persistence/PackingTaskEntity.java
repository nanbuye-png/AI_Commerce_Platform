package com.commerce.platform.warehouse.infrastructure.persistence;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.warehouse.domain.valueobject.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "packing_task", indexes = {
    @Index(name = "idx_pkt_fulfillment_id", columnList = "fulfillment_id"),
    @Index(name = "idx_pkt_picking_task_id", columnList = "picking_task_id"),
    @Index(name = "idx_pkt_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingTaskEntity extends BaseEntity {

    @Column(name = "fulfillment_id", nullable = false)
    private Long fulfillmentId;

    @Column(name = "picking_task_id", nullable = false)
    private Long pickingTaskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.CREATED;

    @Column(name = "packed_at")
    private LocalDateTime packedAt;

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