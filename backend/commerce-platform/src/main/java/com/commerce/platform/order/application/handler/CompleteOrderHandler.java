package com.commerce.platform.order.application.handler;

import com.commerce.platform.order.application.command.CompleteOrderCommand;
import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.event.OrderCompletedEvent;
import com.commerce.platform.order.exception.OrderNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 订单完成命令处理器
 * <p>
 * 接收 CompleteOrderCommand，执行订单完成流程：
 * 1. OrderRepository.findById()
 * 2. Order.complete()（SHIPPED → COMPLETED）
 * 3. OrderRepository.save()
 * 4. 发布 OrderCompletedEvent
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteOrderHandler {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理订单完成命令
     *
     * @param command 订单完成命令
     */
    @Transactional(rollbackOn = Exception.class)
    public void handle(CompleteOrderCommand command) {
        long startTime = System.currentTimeMillis();
        Long orderId = command.getOrderId();
        log.info("开始处理订单完成命令 - orderId={}", orderId);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("订单不存在 - orderId=" + orderId));

            // 幂等处理：如果订单已 COMPLETED，忽略重复请求
            if (order.getOrderStatus() == OrderStatus.COMPLETED) {
                log.warn("订单已完成，忽略重复完成请求 - orderId={}", orderId);
                return;
            }

            // 领域行为：由 Order Aggregate 校验状态并转换
            order.complete();
            orderRepository.save(order);

            // 发布 OrderCompletedEvent
            eventPublisher.publishEvent(new OrderCompletedEvent(
                    order.getOrderNo(), order.getBuyerId()));

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("订单完成处理成功 - orderNo={}, orderId={}, 耗时={}ms",
                    order.getOrderNo(), orderId, elapsed);

        } catch (OrderNotFoundException e) {
            log.error("订单不存在，无法完成 - orderId={}", orderId, e);
            throw e;
        }
    }
}