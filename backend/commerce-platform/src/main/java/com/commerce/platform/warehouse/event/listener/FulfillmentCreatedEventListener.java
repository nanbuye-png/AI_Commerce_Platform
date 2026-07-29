package com.commerce.platform.warehouse.event.listener;

import com.commerce.platform.fulfillment.domain.event.FulfillmentCreatedEvent;
import com.commerce.platform.warehouse.application.command.CreatePickingTaskCommand;
import com.commerce.platform.warehouse.application.handler.CreatePickingTaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 履约单创建事件监听器（属于 Warehouse Domain）
 * <p>
 * 监听 FulfillmentCreatedEvent，创建拣货任务。
 * 链路：FulfillmentCreatedEvent → CreatePickingTaskCommand → CreatePickingTaskHandler → PickingTaskCreatedEvent
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentCreatedEventListener {

    private final CreatePickingTaskHandler createPickingTaskHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFulfillmentCreated(FulfillmentCreatedEvent event) {
        log.info("收到履约单创建事件，开始创建拣货任务: fulfillmentId={}, orderId={}",
                event.getFulfillmentId(), event.getOrderId());

        try {
            CreatePickingTaskCommand command = new CreatePickingTaskCommand(event.getFulfillmentId());
            createPickingTaskHandler.handle(command);

            log.info("拣货任务创建完成: fulfillmentId={}", event.getFulfillmentId());
        } catch (Exception e) {
            log.error("创建拣货任务失败: fulfillmentId={}, error={}",
                    event.getFulfillmentId(), e.getMessage(), e);
        }
    }
}