package com.commerce.platform.inventory.dto.merchant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商家库存调整请求
 */
@Data
public class InventoryAdjustRequest {

    /**
     * 调整类型：INCREASE（增加）/ DECREASE（减少）
     */
    @NotNull(message = "调整类型不能为空")
    private String adjustType;

    /**
     * 调整数量（必须为正整数）
     */
    @NotNull(message = "调整数量不能为空")
    @Min(value = 1, message = "调整数量必须大于0")
    private Integer quantity;

    /**
     * 备注
     */
    private String remark;
}