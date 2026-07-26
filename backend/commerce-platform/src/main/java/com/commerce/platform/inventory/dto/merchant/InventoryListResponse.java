package com.commerce.platform.inventory.dto.merchant;

import lombok.Data;

/**
 * 商家库存列表响应（每项）
 */
@Data
public class InventoryListResponse {

    /**
     * 库存记录 ID
     */
    private Long id;

    /**
     * SKU ID
     */
    private Long productSkuId;

    /**
     * SKU 编码
     */
    private String skuCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 可售库存
     */
    private Integer availableStock;

    /**
     * 已锁定库存
     */
    private Integer reservedStock;

    /**
     * 总库存
     */
    private Integer totalStock;

    /**
     * 是否低于安全库存（low_stock_threshold）
     */
    private Boolean lowStock;
}