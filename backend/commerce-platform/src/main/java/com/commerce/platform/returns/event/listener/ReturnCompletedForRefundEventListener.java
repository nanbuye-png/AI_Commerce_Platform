package com.commerce.platform.returns.event.listener;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.repository.RefundRepository;
import com.commerce.platform.refund.domain.valueobject.RefundReason;
import com.commerce.platform.refund.domain.valueobject.RefundStatus;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.event.ReturnCompletedEvent;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import jakarta.transaction.Transactional;

/**
 * 退货完成事件监听器
 * <p>
 * 监听 ReturnCompletedEvent，触发退款流程。
 * Return 不直接调用 Refund，通过 Event 驱动。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnCompletedForRefundEventListener {

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void onReturnCompleted(ReturnCompletedEvent event) {
        log.info("收到退货完成事件，开始创建退款: returnId={}, orderId={}",
                event.getReturnId(), event.getOrderId());

        try {
            ReturnRequest returnRequest = returnRepository.findById(event.getReturnId())
                    .orElseThrow(() -> new IllegalArgumentException("退货申请不存在"));
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("关联订单不存在"));
            if (refundRepository.findByOrderId(order.getId()).stream()
                    .noneMatch(refund -> refund.getStatus() == RefundStatus.COMPLETED)) {
                Refund refund = Refund.create(order.getId(), returnRequest.getUserId(), order.getPayAmount(), mapRefundReason(returnRequest));
                refund.approve();
                refund.process();
                refund.complete();
                refundRepository.save(refund);
            }
            if (order.getOrderStatus() == com.commerce.platform.order.domain.enums.OrderStatus.REFUNDING) {
                order.completeRefund();
                orderRepository.save(order);
            }

            log.info("退款已触发: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("退货完成事件处理失败: returnId={}, error={}",
                    event.getReturnId(), e.getMessage(), e);
        }
    }

    private RefundReason mapRefundReason(ReturnRequest returnRequest) {
        return switch (returnRequest.getReason()) {
            case QUALITY_ISSUE -> RefundReason.QUALITY_ISSUE;
            case DAMAGED -> RefundReason.DAMAGED;
            case WRONG_ITEM -> RefundReason.WRONG_PRODUCT;
            default -> RefundReason.OTHER;
        };
    }
}
