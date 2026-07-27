package com.commerce.platform.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 锁定库存请求 DTO
 */
@Data
public class LockInventoryRequest {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @Min(value = 1, message = "锁定数量必须大于0")
    @NotNull(message = "锁定数量不能为空")
    private Integer quantity;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}