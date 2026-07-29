package com.commerce.platform.returns.application.handler;

import com.commerce.platform.returns.application.command.CompleteReturnCommand;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.event.ReturnCompletedEvent;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteReturnHandler {
    private final ReturnRepository returnRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackOn = Exception.class)
    public void handle(CompleteReturnCommand command) {
        log.info("开始完成退货 - returnId={}", command.getReturnId());
        ReturnRequest request = returnRepository.findById(command.getReturnId())
                .orElseThrow(() -> new IllegalArgumentException("退货不存在 - returnId=" + command.getReturnId()));
        if (command.getRefundId() != null) {
            request.setRefundId(command.getRefundId());
        }
        request.complete();
        returnRepository.save(request);
        eventPublisher.publishEvent(new ReturnCompletedEvent(request.getId(), request.getOrderId()));
        log.info("退货完成 - returnId={}, orderId={}, refundId={}", request.getId(), request.getOrderId(), request.getRefundId());
    }
}