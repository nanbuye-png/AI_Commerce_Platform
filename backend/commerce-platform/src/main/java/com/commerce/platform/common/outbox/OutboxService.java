package com.commerce.platform.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Service
 * <p>
 * 负责将领域事件保存到 outbox_event 表，与业务数据在同一事务中持久化。
 * 禁止 Domain Entity 直接依赖此类，仅在 ApplicationService 层使用。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * 保存领域事件到 Outbox
     *
     * @param domainEvent  领域事件对象
     * @param aggregateType 聚合类型（如 Order、Payment）
     * @param aggregateId  聚合 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveEvent(Object domainEvent, String aggregateType, String aggregateId) {
        try {
            String eventId = OutboxEvent.generateEventId();
            String eventType = domainEvent.getClass().getName();
            String payload = objectMapper.writeValueAsString(domainEvent);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .payload(payload)
                    .status(OutboxStatus.NEW)
                    .build();

            outboxRepository.save(outboxEvent);

            log.debug("Outbox 事件已保存：eventId={}, eventType={}, aggregateType={}, aggregateId={}",
                    eventId, eventType, aggregateType, aggregateId);

        } catch (Exception e) {
            log.error("Outbox 事件保存失败：eventType={}, error={}", 
                    domainEvent.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("Outbox 事件保存失败", e);
        }
    }

    /**
     * 保存领域事件到 Outbox（无 aggregate 信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveEvent(Object domainEvent) {
        saveEvent(domainEvent, null, null);
    }
}