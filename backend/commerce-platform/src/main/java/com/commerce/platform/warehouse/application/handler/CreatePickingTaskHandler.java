package com.commerce.platform.warehouse.application.handler;

import com.commerce.platform.warehouse.application.command.CreatePickingTaskCommand;
import com.commerce.platform.warehouse.domain.aggregate.PickingTask;
import com.commerce.platform.warehouse.domain.event.PickingTaskCreatedEvent;
import com.commerce.platform.warehouse.domain.repository.PickingTaskRepository;
import com.commerce.platform.warehouse.domain.service.WarehouseDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 创建拣货任务命令处理器
 * <p>
 * 职责链：接收 Command → 调用 Domain Service → 保存 Repository → 发布 Domain Event
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePickingTaskHandler {

    private final WarehouseDomainService warehouseDomainService;
    private final PickingTaskRepository pickingTaskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public PickingTask handle(CreatePickingTaskCommand command) {
        log.info("开始创建拣货任务: fulfillmentId={}", command.getFulfillmentId());

        // 1. 调用 Domain Service 创建拣货任务
        PickingTask pickingTask = warehouseDomainService.createPickingTask(command.getFulfillmentId());

        // 2. 持久化
        PickingTask saved = pickingTaskRepository.save(pickingTask);

        // 3. 发布事件
        PickingTaskCreatedEvent event = new PickingTaskCreatedEvent(
                saved.getId(), saved.getFulfillmentId(), saved.getWarehouseId());
        eventPublisher.publishEvent(event);

        log.info("拣货任务创建成功: pickingTaskId={}, fulfillmentId={}, warehouseId={}",
                saved.getId(), saved.getFulfillmentId(), saved.getWarehouseId());

        return saved;
    }
}