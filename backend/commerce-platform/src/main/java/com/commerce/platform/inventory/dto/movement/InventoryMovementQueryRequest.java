package com.commerce.platform.inventory.dto.movement;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水查询请求
 */
@Data
public class InventoryMovementQueryRequest {

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 20;

    /**
     * SKU ID（按 SKU 筛选）
     */
    private Long productSkuId;

    /**
     * 变动类型
     */
    private String movementType;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 原因码
     */
    private String reasonCode;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}