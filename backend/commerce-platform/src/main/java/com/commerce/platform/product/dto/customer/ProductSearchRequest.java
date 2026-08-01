package com.commerce.platform.product.dto.customer;

import lombok.Data;

import java.math.BigDecimal;

/**
 * C端商品搜索请求 DTO
 */
@Data
public class ProductSearchRequest {

    private int page = 1;
    private int size = 20;
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy = "createdTime";
    private String sortOrder = "desc";
}