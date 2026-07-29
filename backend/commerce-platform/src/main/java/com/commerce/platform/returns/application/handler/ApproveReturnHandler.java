package com.commerce.platform.returns.application.handler;

import com.commerce.platform.returns.application.command.ApproveReturnCommand;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.event.ReturnApprovedEvent;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApproveReturnHandler {
    private final ReturnRepository returnRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackOn = Exception.class)
    public void handle(ApproveReturnCommand command) {
        log.info("开始审核退货 - returnId={}", command.getReturnId());
        ReturnRequest request = returnRepository.findById(command.getReturnId())
                .orElseThrow(() -> new IllegalArgumentException("退货不存在 - returnId=" + command.getReturnId()));
        request.approve();
        returnRepository.save(request);
        eventPublisher.publishEvent(new ReturnApprovedEvent(request.getId(), request.getOrderId()));
        log.info("退货审核通过 - returnId={}, orderId={}", request.getId(), request.getOrderId());
    }
}