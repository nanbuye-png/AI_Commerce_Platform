package com.commerce.platform.inventory.dto.reservation;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存预占详情响应
 */
@Data
public class ReservationDetailResponse {

    /**
     * 预占编号
     */
    private String reservationNo;

    /**
     * 库存记录 ID
     */
    private Long inventoryId;

    /**
     * SKU ID
     */
    private Long productSkuId;

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 锁定数量
     */
    private Integer quantity;

    /**
     * 预占状态
     */
    private String status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}