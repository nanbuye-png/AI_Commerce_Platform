package com.commerce.platform.product.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品SKU请求 DTO
 */
@Data
public class ProductSkuRequest {

    @NotBlank(message = "SKU编码不能为空")
    @Size(max = 64, message = "SKU编码最长64个字符")
    private String skuCode;

    @NotNull(message = "规格属性不能为空")
    private String attributesJson;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal weight;

    private Integer stock;
}