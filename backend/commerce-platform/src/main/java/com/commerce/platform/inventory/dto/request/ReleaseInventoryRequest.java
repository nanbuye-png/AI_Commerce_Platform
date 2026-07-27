package com.commerce.platform.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 释放库存请求 DTO
 */
@Data
public class ReleaseInventoryRequest {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "释放数量不能为空")
    private Integer quantity;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}