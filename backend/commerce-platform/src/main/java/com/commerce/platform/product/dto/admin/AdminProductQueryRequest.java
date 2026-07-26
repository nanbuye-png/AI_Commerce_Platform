package com.commerce.platform.product.dto.admin;

import lombok.Data;

/**
 * Admin 商品列表查询请求 DTO
 */
@Data
public class AdminProductQueryRequest {

    private int page = 1;
    private int size = 20;
    private String status;
    private Long merchantId;
    private Long categoryId;
    private String keyword;
}