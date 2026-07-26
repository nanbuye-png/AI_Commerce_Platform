package com.commerce.platform.inventory.dto.merchant;

import lombok.Data;

/**
 * 商家库存查询请求
 */
@Data
public class InventoryQueryRequest {

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 20;

    /**
     * SKU 编码（模糊搜索）
     */
    private String skuCode;

    /**
     * 商品名称（模糊搜索）
     */
    private String productName;
}