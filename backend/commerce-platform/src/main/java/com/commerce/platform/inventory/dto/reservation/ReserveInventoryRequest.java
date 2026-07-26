package com.commerce.platform.inventory.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 库存锁定请求
 */
@Data
public class ReserveInventoryRequest {

    /**
     * 库存记录 ID
     */
    @NotNull(message = "库存记录ID不能为空")
    private Long inventoryId;

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    private Long productSkuId;

    /**
     * 订单 ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 锁定数量
     */
    @NotNull(message = "锁定数量不能为空")
    @Min(value = 1, message = "锁定数量必须大于0")
    private Integer quantity;

    /**
     * 过期时间（单位：分钟），默认30分钟
     */
    private Integer expireMinutes = 30;
}