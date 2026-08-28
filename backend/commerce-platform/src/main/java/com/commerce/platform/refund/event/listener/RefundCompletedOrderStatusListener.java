package com.commerce.platform.refund.event.listener;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.refund.domain.event.RefundCompletedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 退款完成后同步订单主状态。
 *
 * 退款可能由命令处理器、商家接口或其他支付通道完成，订单状态同步不能依赖单一入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundCompletedOrderStatusListener {

    private final OrderRepository orderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void onRefundCompleted(RefundCompletedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (order.getOrderStatus() == OrderStatus.REFUNDING) {
                order.completeRefund();
                orderRepository.save(order);
                log.info("退款完成后订单状态已同步 - refundId={}, orderId={}", event.getRefundId(), event.getOrderId());
            }
        });
    }
}
