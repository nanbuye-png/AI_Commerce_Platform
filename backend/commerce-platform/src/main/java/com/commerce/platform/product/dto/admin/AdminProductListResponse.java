package com.commerce.platform.product.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Admin 商品列表响应 DTO
 */
@Data
public class AdminProductListResponse {

    private Long id;
    private String productCode;
    private String productName;
    private String brand;
    private Long categoryId;
    private Long merchantId;
    private String merchantName;
    private String status;
    private Integer salesCount;
    private String coverImage;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}