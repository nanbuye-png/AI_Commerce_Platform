package com.commerce.platform.product.dto.customer;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C端商品卡片响应 DTO（列表用）
 */
@Data
public class ProductCardResponse {

    private Long id;
    private String productName;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String coverImage;
    private Integer salesCount;
    private LocalDateTime createdTime;
}