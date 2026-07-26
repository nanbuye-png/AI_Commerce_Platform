package com.commerce.platform.inventory.dto.reservation;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存预占操作响应
 */
@Data
public class ReservationResponse {

    /**
     * 预占编号
     */
    private String reservationNo;

    /**
     * 预占状态
     */
    private String status;

    /**
     * 锁定数量
     */
    private Integer quantity;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}