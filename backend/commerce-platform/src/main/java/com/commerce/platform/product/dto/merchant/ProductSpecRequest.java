package com.commerce.platform.product.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品规格模板请求 DTO
 */
@Data
public class ProductSpecRequest {

    @NotBlank(message = "规格名称不能为空")
    @Size(max = 64, message = "规格名称最长64个字符")
    private String specName;

    @NotBlank(message = "规格值不能为空")
    private String specValues;

    private Integer sort;
}