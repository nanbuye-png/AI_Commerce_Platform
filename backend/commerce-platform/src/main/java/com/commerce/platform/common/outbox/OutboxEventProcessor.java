package com.commerce.platform.common.outbox;

import com.commerce.platform.cart.event.CartCheckedOutEvent;
import com.commerce.platform.inventory.event.InventoryDeductedEvent;
import com.commerce.platform.inventory.event.InventoryLockedEvent;
import com.commerce.platform.order.event.OrderCreatedEvent;
import com.commerce.platform.order.event.OrderPaidEvent;
import com.commerce.platform.payment.event.OrderCreatedForPaymentEvent;
import com.commerce.platform.payment.event.PaymentSuccessEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbox 事件处理器
 * <p>
 * 定时轮询 outbox_event 表，将 NEW 状态的事件反序列化后发布到 Spring EventBus。
 * 支持重试机制（最多 5 次）。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 事件类型 → Class 映射表
     */
    private static final Map<String, Class<?>> EVENT_TYPE_MAP = new ConcurrentHashMap<>();

    static {
        EVENT_TYPE_MAP.put("com.commerce.platform.cart.event.CartCheckedOutEvent", CartCheckedOutEvent.class);
        EVENT_TYPE_MAP.put("com.commerce.platform.order.event.OrderCreatedEvent", OrderCreatedEvent.class);
        EVENT_TYPE_MAP.put("com.commerce.platform.order.event.OrderPaidEvent", OrderPaidEvent.class);
        EVENT_TYPE_MAP.put("com.commerce.platform.payment.event.PaymentSuccessEvent", PaymentSuccessEvent.class);
        EVENT_TYPE_MAP.put("com.commerce.platform.payment.event.OrderCreatedForPaymentEvent", OrderCreatedForPaymentEvent.class);
        EVENT_TYPE_MAP.put("com.commerce.platform.inventory.event.InventoryLockedEvent", InventoryLockedEvent.class);
        EVENT_TYPE_MAP.put("com.commerce.platform.inventory.event.InventoryDeductedEvent", InventoryDeductedEvent.class);
    }

    /**
     * 每 5 秒轮询一次 NEW 状态的事件
     */
    @Scheduled(fixedDelay = 5000)
    public void processPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxStatus.NEW);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Outbox 处理器开始轮询，发现 {} 个待处理事件", pendingEvents.size());

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                processEvent(outboxEvent);
            } catch (Exception e) {
                log.error("Outbox 事件处理异常：eventId={}, eventType={}, error={}",
                        outboxEvent.getEventId(), outboxEvent.getEventType(), e.getMessage(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processEvent(OutboxEvent outboxEvent) {
        // 标记为 PROCESSING
        outboxEvent.markProcessing();
        outboxRepository.save(outboxEvent);

        try {
            // 反序列化事件
            Class<?> eventClass = EVENT_TYPE_MAP.get(outboxEvent.getEventType());
            if (eventClass == null) {
                log.warn("未知事件类型，标记为 FAILED：eventType={}", outboxEvent.getEventType());
                outboxEvent.markFailed("未知事件类型: " + outboxEvent.getEventType());
                outboxRepository.save(outboxEvent);
                return;
            }

            Object event = objectMapper.readValue(outboxEvent.getPayload(), eventClass);

            // 发布到 Spring EventBus
            eventPublisher.publishEvent(event);

            // 标记为 SUCCESS
            outboxEvent.markSuccess();
            outboxRepository.save(outboxEvent);

            log.info("Outbox 事件投递成功：eventId={}, eventType={}", 
                    outboxEvent.getEventId(), outboxEvent.getEventType());

        } catch (Exception e) {
            log.error("Outbox 事件处理失败：eventId={}, eventType={}, error={}",
                    outboxEvent.getEventId(), outboxEvent.getEventType(), e.getMessage(), e);

            outboxEvent.markFailed(e.getMessage());
            outboxRepository.save(outboxEvent);

            // 如果超过最大重试次数，不再重试
            if (!outboxEvent.shouldRetry()) {
                log.warn("Outbox 事件已达最大重试次数：eventId={}, eventType={}",
                        outboxEvent.getEventId(), outboxEvent.getEventType());
            }
        }
    }
}