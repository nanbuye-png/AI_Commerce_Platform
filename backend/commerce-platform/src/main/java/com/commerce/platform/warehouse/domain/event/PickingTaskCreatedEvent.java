package com.commerce.platform.warehouse.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 拣货任务创建事件
 * <p>
 * 当拣货任务创建后发布。
 * </p>
 */
@Getter
public class PickingTaskCreatedEvent {

    private final Long pickingTaskId;
    private final Long fulfillmentId;
    private final Long warehouseId;
    private final LocalDateTime occurredOn;

    public PickingTaskCreatedEvent(Long pickingTaskId, Long fulfillmentId, Long warehouseId) {
        this.pickingTaskId = pickingTaskId;
        this.fulfillmentId = fulfillmentId;
        this.warehouseId = warehouseId;
        this.occurredOn = LocalDateTime.now();
    }
}