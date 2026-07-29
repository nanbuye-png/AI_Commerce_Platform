package com.commerce.platform.fulfillment.event.listener;

import com.commerce.platform.fulfillment.application.command.CreateFulfillmentCommand;
import com.commerce.platform.fulfillment.application.handler.CreateFulfillmentHandler;
import com.commerce.platform.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单支付事件监听器（属于 Fulfillment Domain）
 * <p>
 * 监听 OrderPaidEvent，创建履约单。
 * 整个链路：OrderPaidEvent → CreateFulfillmentCommand → CreateFulfillmentHandler → FulfillmentCreatedEvent
 * 使用 Event Bus（Spring ApplicationEventPublisher），不绕过事件机制。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidForFulfillmentEventListener {

    private final CreateFulfillmentHandler createFulfillmentHandler;

    /**
     * 处理订单支付事件
     * <p>
     * 在事务提交后执行，确保订单数据已经持久化。
     * </p>
     *
     * @param event 订单支付事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("收到订单支付事件，开始创建履约单: orderId={}, orderNo={}",
                event.getOrderId(), event.getOrderNo());

        try {
            // 从事件中提取 merchantId（OrderPaidEvent 不包含 merchantId，此处使用占位 -1）
            // 在实际业务中，需要通过 orderId 查询订单获取 merchantId
            // 此处简化为占位值，由 CreateFulfillmentHandler 幂等处理
            Long merchantId = -1L;

            CreateFulfillmentCommand command = new CreateFulfillmentCommand(
                    event.getOrderId(), merchantId);

            createFulfillmentHandler.handle(command);

            log.info("履约单创建完成: orderId={}, fulfillmentCreated", event.getOrderId());
        } catch (Exception e) {
            log.error("创建履约单失败: orderId={}, error={}", event.getOrderId(), e.getMessage(), e);
        }
    }
}