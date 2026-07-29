package com.commerce.platform.warehouse.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 打包完成事件
 * <p>
 * 当打包任务完成后发布，用于触发履约单标记等待发货。
 * </p>
 */
@Getter
public class PackingCompletedEvent {

    private final Long packingTaskId;
    private final Long fulfillmentId;
    private final Long pickingTaskId;
    private final LocalDateTime occurredOn;

    public PackingCompletedEvent(Long packingTaskId, Long fulfillmentId, Long pickingTaskId) {
        this.packingTaskId = packingTaskId;
        this.fulfillmentId = fulfillmentId;
        this.pickingTaskId = pickingTaskId;
        this.occurredOn = LocalDateTime.now();
    }
}