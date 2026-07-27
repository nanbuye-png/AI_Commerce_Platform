package com.commerce.platform.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建库存请求 DTO
 */
@Data
public class CreateInventoryRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @Min(value = 0, message = "初始库存不能小于0")
    private Integer initialStock;
}