package com.commerce.platform.refund.application.handler;

import com.commerce.platform.refund.application.command.CreateRefundCommand;
import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.event.RefundCreatedEvent;
import com.commerce.platform.refund.domain.repository.RefundRepository;
import com.commerce.platform.refund.domain.service.RefundDomainService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 创建退款命令处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRefundHandler {

    private final RefundDomainService refundDomainService;
    private final RefundRepository refundRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackOn = Exception.class)
    public void handle(CreateRefundCommand command) {
        long startTime = System.currentTimeMillis();
        log.info("开始创建退款 - orderId={}, userId={}, amount={}, reason={}",
                command.getOrderId(), command.getUserId(), command.getAmount(), command.getReason());

        Refund refund = refundDomainService.createRefund(
                command.getOrderId(), command.getUserId(),
                command.getAmount(), command.getReason());

        Refund saved = refundRepository.save(refund);

        eventPublisher.publishEvent(new RefundCreatedEvent(
                saved.getId(), saved.getOrderId(), saved.getUserId()));

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("退款创建成功 - refundId={}, orderId={}, 耗时={}ms",
                saved.getId(), saved.getOrderId(), elapsed);
    }
}