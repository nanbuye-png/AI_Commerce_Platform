package com.commerce.platform.order.event.listener;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.event.OrderPaidEvent;
import com.commerce.platform.order.exception.OrderNotFoundException;
import com.commerce.platform.payment.event.PaymentSuccessEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 支付事件监听器（属于 Order Domain）
 * <p>
 * 监听 Payment Domain 发布的事件，驱动 Order 状态流转。
 * Payment 不依赖 Order，Order 不依赖 Payment，通过 Spring Event 解耦。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 监听支付成功事件
     * <p>
     * 流程：
     * 1. 接收 PaymentSuccessEvent
     * 2. 获取 orderNo
     * 3. 查询 Order
     * 4. Order.pay()（PENDING_PAYMENT → PAID）
     * 5. 保存 Order
     * 6. 发布 OrderPaidEvent
     * </p>
     */
    @Transactional(rollbackOn = Exception.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        long startTime = System.currentTimeMillis();
        log.info("收到支付成功事件 - paymentNo={}, orderNo={}, transactionNo={}",
                event.getPaymentNo(), event.getOrderNo(), event.getTransactionNo());

        try {
            Order order = orderRepository.findByOrderNo(event.getOrderNo())
                    .orElseThrow(() -> new OrderNotFoundException(event.getOrderNo()));

            // 幂等处理：如果订单已支付，忽略重复事件
            if (order.getOrderStatus() == OrderStatus.PAID) {
                log.warn("订单已支付，忽略重复事件 - orderNo={}, paymentNo={}",
                        event.getOrderNo(), event.getPaymentNo());
                return;
            }

            // 领域行为：由 Order Entity 校验状态并转换
            order.pay();
            orderRepository.save(order);

            // 发布 OrderPaidEvent
            eventPublisher.publishEvent(new OrderPaidEvent(
                    order.getId(), order.getOrderNo(), event.getPaymentNo()));

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("订单支付成功 - orderNo={}, paymentNo={}, 耗时={}ms",
                    event.getOrderNo(), event.getPaymentNo(), elapsed);

        } catch (OrderNotFoundException e) {
            log.error("订单不存在，支付成功事件无法处理 - orderNo={}, paymentNo={}",
                    event.getOrderNo(), event.getPaymentNo(), e);
            throw e;
        }
    }
}