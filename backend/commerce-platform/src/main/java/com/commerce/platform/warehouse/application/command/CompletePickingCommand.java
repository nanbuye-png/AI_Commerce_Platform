package com.commerce.platform.warehouse.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 完成拣货命令
 */
@Getter
public class CompletePickingCommand {

    @NotNull
    private final Long pickingTaskId;

    public CompletePickingCommand(Long pickingTaskId) {
        this.pickingTaskId = pickingTaskId;
    }
}