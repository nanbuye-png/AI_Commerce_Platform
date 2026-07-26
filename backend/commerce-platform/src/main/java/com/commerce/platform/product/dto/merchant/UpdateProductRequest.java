package com.commerce.platform.product.dto.merchant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 商家更新商品请求 DTO
 */
@Data
public class UpdateProductRequest {

    @Size(max = 256, message = "商品名称最长256个字符")
    private String productName;

    @Size(max = 10000, message = "商品描述最长10000个字符")
    private String description;

    @Size(max = 64, message = "品牌名称最长64个字符")
    private String brand;

    private Long categoryId;

    /**
     * 商品图片列表（全量替换）
     */
    private List<@Valid ProductImageRequest> images;

    /**
     * 商品规格模板列表（全量替换）
     */
    private List<@Valid ProductSpecRequest> specs;

    /**
     * 商品SKU列表（全量替换）
     */
    private List<@Valid ProductSkuRequest> skus;
}