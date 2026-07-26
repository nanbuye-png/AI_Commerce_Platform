package com.commerce.platform.inventory.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 扣减库存预留请求（支付成功后调用）
 */
@Data
public class DeductReservationRequest {

    /**
     * 预占编号
     */
    @NotNull(message = "预占编号不能为空")
    private String reservationNo;

    /**
     * 扣减数量（支持部分扣减）
     */
    @NotNull(message = "扣减数量不能为空")
    @Min(value = 1, message = "扣减数量必须大于0")
    private Integer quantity;

    /**
     * 备注
     */
    private String remark;
}