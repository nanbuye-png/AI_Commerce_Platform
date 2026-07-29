package com.commerce.platform.inventory.reservation.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 确认库存命令
 * <p>
 * 接收来自事件或API的库存确认请求。
 * </p>
 */
@Getter
public class ConfirmStockCommand {

    /** 预占ID */
    @NotNull(message = "预占ID不能为空")
    private final Long reservationId;

    /**
     * 构造确认库存命令
     *
     * @param reservationId 预占ID
     */
    public ConfirmStockCommand(Long reservationId) {
        this.reservationId = reservationId;
    }
}