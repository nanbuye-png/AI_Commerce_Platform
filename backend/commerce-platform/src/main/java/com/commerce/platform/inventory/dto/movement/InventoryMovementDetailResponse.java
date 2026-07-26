package com.commerce.platform.inventory.dto.movement;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水详情响应（完整审计链路）
 */
@Data
public class InventoryMovementDetailResponse {

    private String movementNo;
    private Long productSkuId;
    private Long inventoryId;
    private String movementType;
    private String sourceType;
    private String sourceId;
    private String reasonCode;
    private Integer quantity;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private Integer beforeReserved;
    private Integer afterReserved;
    private Long operatorId;
    private String operatorName;
    private String businessId;
    private String remark;
    private LocalDateTime createdTime;
}