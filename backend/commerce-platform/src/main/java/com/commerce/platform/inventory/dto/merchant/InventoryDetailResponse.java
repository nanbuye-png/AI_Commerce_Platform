package com.commerce.platform.inventory.dto.merchant;

import lombok.Data;

/**
 * 商家库存详情响应
 */
@Data
public class InventoryDetailResponse {

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
     * 低库存阈值
     */
    private Integer lowStockThreshold;
}