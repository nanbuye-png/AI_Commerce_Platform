package com.commerce.platform.order.event.listener;

import com.commerce.platform.fulfillment.domain.repository.FulfillmentRepository;
import com.commerce.platform.order.application.command.CompleteOrderCommand;
import com.commerce.platform.order.application.handler.CompleteOrderHandler;
import com.commerce.platform.shipping.domain.event.ShipmentDeliveredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 物流送达事件监听器（属于 Order Domain）
 * <p>
 * 监听 Shipping Domain 的 ShipmentDeliveredEvent，驱动订单完成。
 * 整个链路：
 * ShipmentDeliveredEvent → CompleteOrderCommand → CompleteOrderHandler → OrderCompletedEvent
 * <p>
 * Shipping 不依赖 Order，Order 不依赖 Shipping，通过 Spring Event 解耦。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentDeliveredEventListener {

    private final FulfillmentRepository fulfillmentRepository;
    private final CompleteOrderHandler completeOrderHandler;

    /**
     * 处理物流送达事件
     * <p>
     * 在事务提交后执行，确保物流数据已经持久化。
     * 通过 Fulfillment 获取关联的订单 ID，然后驱动订单完成。
     * </p>
     *
     * @param event 物流送达事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShipmentDelivered(ShipmentDeliveredEvent event) {
        log.info("收到物流送达事件，开始完成订单: shipmentId={}, fulfillmentId={}",
                event.getShipmentId(), event.getFulfillmentId());

        try {
            // 通过履约单获取关联的订单 ID
            Long orderId = fulfillmentRepository.findById(event.getFulfillmentId())
                    .map(fulfillment -> fulfillment.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "履约单不存在 - fulfillmentId=" + event.getFulfillmentId()));

            // 创建订单完成命令并执行
            CompleteOrderCommand command = new CompleteOrderCommand(orderId);
            completeOrderHandler.handle(command);

            log.info("订单完成已触发: fulfillmentId={}, orderId={}",
                    event.getFulfillmentId(), orderId);
        } catch (Exception e) {
            log.error("物流送达事件处理失败: shipmentId={}, fulfillmentId={}, error={}",
                    event.getShipmentId(), event.getFulfillmentId(), e.getMessage(), e);
        }
    }
}