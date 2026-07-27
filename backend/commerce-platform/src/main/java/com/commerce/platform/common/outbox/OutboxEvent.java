package com.commerce.platform.common.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbox 事件记录
 * <p>
 * 用于可靠投递领域事件。业务数据与 OutboxEvent 在同一事务中持久化，
 * 由 OutboxEventProcessor 定时轮询并投递到 Spring EventBus。
 * </p>
 */
@Entity
@Table(name = "outbox_event", indexes = {
    @Index(name = "idx_outbox_status", columnList = "status"),
    @Index(name = "idx_outbox_event_id", columnList = "event_id", unique = true)
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    @Column(name = "aggregate_type", length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.PRIVATE)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.NEW;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "processed_time")
    private LocalDateTime processedTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
    }

    // ============================================
    // 领域行为
    // 禁止外部直接修改 status
    // ============================================

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    public void markSuccess() {
        this.status = OutboxStatus.SUCCESS;
        this.processedTime = LocalDateTime.now();
    }

    public void markFailed(String errorMsg) {
        this.status = OutboxStatus.FAILED;
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
        this.processedTime = LocalDateTime.now();
    }

    /**
     * 是否需要重试（最多 5 次）
     */
    public boolean shouldRetry() {
        return (this.retryCount == null ? 0 : this.retryCount) < 5;
    }

    /**
     * 生成唯一 EventId
     */
    public static String generateEventId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}