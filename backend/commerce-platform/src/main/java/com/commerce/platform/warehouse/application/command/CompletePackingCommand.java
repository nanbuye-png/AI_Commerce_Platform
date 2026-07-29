package com.commerce.platform.warehouse.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 完成打包命令
 */
@Getter
public class CompletePackingCommand {

    @NotNull
    private final Long packingTaskId;

    public CompletePackingCommand(Long packingTaskId) {
        this.packingTaskId = packingTaskId;
    }
}