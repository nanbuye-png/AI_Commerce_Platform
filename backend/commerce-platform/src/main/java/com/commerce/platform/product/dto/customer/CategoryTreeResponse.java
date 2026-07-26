package com.commerce.platform.product.dto.customer;

import lombok.Data;

import java.util.List;

/**
 * 分类树响应 DTO（递归结构）
 */
@Data
public class CategoryTreeResponse {

    private Long id;
    private String categoryName;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private List<CategoryTreeResponse> children;
}