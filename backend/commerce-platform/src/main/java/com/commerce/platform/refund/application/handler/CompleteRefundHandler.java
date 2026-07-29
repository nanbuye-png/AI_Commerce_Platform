package com.commerce.platform.refund.application.handler;

import com.commerce.platform.refund.application.command.CompleteRefundCommand;
import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.event.RefundCompletedEvent;
import com.commerce.platform.refund.domain.repository.RefundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 完成退款命令处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteRefundHandler {

    private final RefundRepository refundRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackOn = Exception.class)
    public void handle(CompleteRefundCommand command) {
        long startTime = System.currentTimeMillis();
        Long refundId = command.getRefundId();
        log.info("开始完成退款 - refundId={}", refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("退款不存在 - refundId=" + refundId));

        refund.complete();
        refundRepository.save(refund);

        eventPublisher.publishEvent(new RefundCompletedEvent(
                refund.getId(), refund.getOrderId()));

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("退款完成成功 - refundId={}, orderId={}, 耗时={}ms",
                refund.getId(), refund.getOrderId(), elapsed);
    }
}