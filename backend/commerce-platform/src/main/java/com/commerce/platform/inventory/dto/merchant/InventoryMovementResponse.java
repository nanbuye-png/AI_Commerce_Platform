package com.commerce.platform.inventory.dto.merchant;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家库存流水响应
 */
@Data
public class InventoryMovementResponse {

    /**
     * 流水编号
     */
    private String movementNo;

    /**
     * SKU ID
     */
    private Long productSkuId;

    /**
     * 变动类型
     */
    private String movementType;

    /**
     * 变动数量
     */
    private Integer quantity;

    /**
     * 变动前可售库存
     */
    private Integer beforeAvailable;

    /**
     * 变动后可售库存
     */
    private Integer afterAvailable;

    /**
     * 操作人 ID
     */
    private Long operatorId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}