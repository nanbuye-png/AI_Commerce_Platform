package com.commerce.platform.product.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品图片请求 DTO
 */
@Data
public class ProductImageRequest {

    @NotBlank(message = "图片URL不能为空")
    @Size(max = 512, message = "图片URL最长512个字符")
    private String url;

    @NotBlank(message = "图片类型不能为空")
    private String imageType;

    private Integer sort;

    private Boolean isCover;
}