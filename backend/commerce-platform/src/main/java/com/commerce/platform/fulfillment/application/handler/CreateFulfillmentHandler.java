package com.commerce.platform.fulfillment.application.handler;

import com.commerce.platform.fulfillment.application.command.CreateFulfillmentCommand;
import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.event.FulfillmentCreatedEvent;
import com.commerce.platform.fulfillment.domain.repository.FulfillmentRepository;
import com.commerce.platform.fulfillment.domain.service.FulfillmentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 创建履约单命令处理器
 * <p>
 * 职责链：接收 Command → 调用 Domain Service → 保存 Repository → 发布 Domain Event
 * 不得直接修改 Aggregate 属性。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateFulfillmentHandler {

    private final FulfillmentDomainService fulfillmentDomainService;
    private final FulfillmentRepository fulfillmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理创建履约单命令
     *
     * @param command 创建履约单命令
     * @return 已创建并持久化的履约单
     */
    @Transactional(rollbackFor = Exception.class)
    public Fulfillment handle(CreateFulfillmentCommand command) {
        log.info("开始创建履约单: orderId={}, merchantId={}", command.getOrderId(), command.getMerchantId());

        // 1. 调用 Domain Service 校验并创建履约单
        Fulfillment fulfillment = fulfillmentDomainService.createFulfillment(
                command.getOrderId(), command.getMerchantId());

        // 2. 保存 Repository
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);

        // 3. 发布 FulfillmentCreatedEvent
        FulfillmentCreatedEvent event = new FulfillmentCreatedEvent(
                savedFulfillment.getId(), savedFulfillment.getOrderId());
        eventPublisher.publishEvent(event);

        log.info("履约单创建成功: fulfillmentId={}, orderId={}",
                savedFulfillment.getId(), savedFulfillment.getOrderId());

        return savedFulfillment;
    }
}