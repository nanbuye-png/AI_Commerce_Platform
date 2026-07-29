package com.commerce.platform.returns.application.handler;

import com.commerce.platform.returns.application.command.CreateReturnCommand;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.event.ReturnCreatedEvent;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import com.commerce.platform.returns.domain.service.ReturnDomainService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateReturnHandler {
    private final ReturnDomainService returnDomainService;
    private final ReturnRepository returnRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackOn = Exception.class)
    public void handle(CreateReturnCommand command) {
        log.info("开始创建退货 - orderId={}, userId={}, reason={}", command.getOrderId(), command.getUserId(), command.getReason());
        ReturnRequest request = returnDomainService.createReturn(command.getOrderId(), command.getUserId(), command.getReason());
        ReturnRequest saved = returnRepository.save(request);
        eventPublisher.publishEvent(new ReturnCreatedEvent(saved.getId(), saved.getOrderId(), saved.getUserId()));
        log.info("退货创建成功 - returnId={}, orderId={}", saved.getId(), saved.getOrderId());
    }
}