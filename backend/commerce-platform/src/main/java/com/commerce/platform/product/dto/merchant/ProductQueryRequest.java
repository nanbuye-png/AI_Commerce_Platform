package com.commerce.platform.product.dto.merchant;

import lombok.Data;

/**
 * 商家商品列表查询请求 DTO
 */
@Data
public class ProductQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String status;

    private String keyword;
}