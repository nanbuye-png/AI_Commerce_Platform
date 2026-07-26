package com.commerce.platform.inventory.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 释放库存预留请求
 */
@Data
public class ReleaseReservationRequest {

    /**
     * 预占编号
     */
    @NotNull(message = "预占编号不能为空")
    private String reservationNo;

    /**
     * 释放数量（支持部分释放）
     */
    @NotNull(message = "释放数量不能为空")
    @Min(value = 1, message = "释放数量必须大于0")
    private Integer quantity;

    /**
     * 备注（如：取消订单、售后释放等）
     */
    private String remark;
}