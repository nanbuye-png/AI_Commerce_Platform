package com.commerce.platform.shipping.event.listener;

import com.commerce.platform.shipping.application.command.CreateShipmentCommand;
import com.commerce.platform.shipping.application.handler.CreateShipmentHandler;
import com.commerce.platform.warehouse.domain.event.PackingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 打包完成事件监听器（属于 Shipping Domain）
 * <p>
 * 监听 PackingCompletedEvent，创建配送单。
 * 链路：PackingCompletedEvent → CreateShipmentCommand → CreateShipmentHandler → ShipmentCreatedEvent
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PackingCompletedEventListener {

    private final CreateShipmentHandler createShipmentHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPackingCompleted(PackingCompletedEvent event) {
        log.info("收到打包完成事件，开始创建配送单: fulfillmentId={}, packingTaskId={}",
                event.getFulfillmentId(), event.getPackingTaskId());

        try {
            // 使用默认承运商"SYSTEM"，实际业务中可从配置或规则引擎获取
            CreateShipmentCommand command = new CreateShipmentCommand(
                    event.getFulfillmentId(), event.getPackingTaskId(), "SYSTEM");
            createShipmentHandler.handle(command);

            log.info("配送单创建完成: fulfillmentId={}", event.getFulfillmentId());
        } catch (Exception e) {
            log.error("创建配送单失败: fulfillmentId={}, error={}",
                    event.getFulfillmentId(), e.getMessage(), e);
        }
    }
}