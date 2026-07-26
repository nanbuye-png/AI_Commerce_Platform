package com.commerce.platform.product.dto.merchant;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家端商品列表响应 DTO
 */
@Data
public class ProductListResponse {

    private Long id;
    private String productCode;
    private String productName;
    private String brand;
    private Long categoryId;
    private String status;
    private Integer salesCount;
    private String coverImage;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}