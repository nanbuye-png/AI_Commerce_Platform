package com.commerce.platform.warehouse.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 创建拣货任务命令
 */
@Getter
public class CreatePickingTaskCommand {

    @NotNull
    private final Long fulfillmentId;

    public CreatePickingTaskCommand(Long fulfillmentId) {
        this.fulfillmentId = fulfillmentId;
    }
}