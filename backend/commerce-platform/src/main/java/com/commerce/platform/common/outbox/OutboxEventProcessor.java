package com.commerce.platform.common.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbox 事件处理器
 * <p>
 * 协调事件领取、事务化投递和结果持久化。每个阶段由独立 Spring Bean 提供事务边界，
 * 避免调度器自调用绕过事务代理。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventTransactionService transactionService;
    private final OutboxEventDispatcher dispatcher;

    public void processPendingEvents() {
        List<OutboxEvent> claimedEvents = transactionService.claimPendingEvents();
        if (claimedEvents.isEmpty()) {
            return;
        }
        log.info("Outbox 处理器开始轮询，领取 {} 个待处理事件", claimedEvents.size());
        for (OutboxEvent event : claimedEvents) {
            processEvent(event);
        }
    }

    void processEvent(OutboxEvent outboxEvent) {
        try {
            dispatcher.dispatch(outboxEvent);
            transactionService.markSuccess(outboxEvent.getId(), outboxEvent.getProcessingToken());
            log.info("Outbox 事件投递成功：eventId={}, eventType={}",
                    outboxEvent.getEventId(), outboxEvent.getEventType());
        } catch (Exception exception) {
            transactionService.markFailed(outboxEvent.getId(), outboxEvent.getProcessingToken(), exception.getMessage());
            log.error("Outbox 事件处理失败：eventId={}, eventType={}, error={}",
                    outboxEvent.getEventId(), outboxEvent.getEventType(), exception.getMessage(), exception);
        }
    }
}