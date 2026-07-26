package com.commerce.platform.inventory.dto.movement;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水列表响应
 */
@Data
public class InventoryMovementResponse {

    private String movementNo;
    private Long productSkuId;
    private String movementType;
    private String sourceType;
    private String sourceId;
    private String reasonCode;
    private Integer quantity;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private String operatorName;
    private String remark;
    private LocalDateTime createdTime;
}