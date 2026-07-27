package com.commerce.platform.common.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 已处理事件记录（用于事件消费幂等）
 * <p>
 * 记录每个 Listener 已成功处理的事件 ID，防止重复消费。
 * </p>
 */
@Entity
@Table(name = "processed_event", uniqueConstraints = {
    @UniqueConstraint(name = "uk_processed_event", columnNames = {"event_id", "consumer_name"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "consumer_name", nullable = false, length = 255)
    private String consumerName;

    @Column(name = "processed_time")
    private LocalDateTime processedTime;

    @PrePersist
    protected void onCreate() {
        this.processedTime = LocalDateTime.now();
    }
}