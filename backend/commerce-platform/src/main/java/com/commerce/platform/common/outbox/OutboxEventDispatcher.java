package com.commerce.platform.common.outbox;

import com.commerce.platform.cart.event.CartCheckedOutEvent;
import com.commerce.platform.inventory.event.InventoryDeductedEvent;
import com.commerce.platform.inventory.event.InventoryLockedEvent;
import com.commerce.platform.order.event.OrderCreatedEvent;
import com.commerce.platform.order.event.OrderPaidEvent;
import com.commerce.platform.payment.domain.event.PaymentSuccessEvent;
import com.commerce.platform.payment.event.OrderCreatedForPaymentEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboxEventDispatcher {

    private static final Map<String, Class<?>> EVENT_TYPES = Map.of(
            CartCheckedOutEvent.class.getName(), CartCheckedOutEvent.class,
            OrderCreatedEvent.class.getName(), OrderCreatedEvent.class,
            OrderPaidEvent.class.getName(), OrderPaidEvent.class,
            PaymentSuccessEvent.class.getName(), PaymentSuccessEvent.class,
            OrderCreatedForPaymentEvent.class.getName(), OrderCreatedForPaymentEvent.class,
            InventoryLockedEvent.class.getName(), InventoryLockedEvent.class,
            InventoryDeductedEvent.class.getName(), InventoryDeductedEvent.class);

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 独立事务确保 AFTER_COMMIT 监听器在该方法返回前完成执行。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(OutboxEvent outboxEvent) throws JsonProcessingException {
        Class<?> eventClass = EVENT_TYPES.get(outboxEvent.getEventType());
        if (eventClass == null) {
            throw new IllegalArgumentException("未知事件类型: " + outboxEvent.getEventType());
        }
        eventPublisher.publishEvent(objectMapper.readValue(outboxEvent.getPayload(), eventClass));
    }
}