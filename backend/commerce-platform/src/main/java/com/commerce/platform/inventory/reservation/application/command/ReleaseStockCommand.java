package com.commerce.platform.inventory.reservation.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 释放库存命令
 * <p>
 * 接收来自事件或API的库存释放请求。
 * </p>
 */
@Getter
public class ReleaseStockCommand {

    /** 预占ID */
    @NotNull(message = "预占ID不能为空")
    private final Long reservationId;

    /**
     * 构造释放库存命令
     *
     * @param reservationId 预占ID
     */
    public ReleaseStockCommand(Long reservationId) {
        this.reservationId = reservationId;
    }
}