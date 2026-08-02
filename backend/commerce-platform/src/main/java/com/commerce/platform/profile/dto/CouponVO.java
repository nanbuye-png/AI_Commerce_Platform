package com.commerce.platform.profile.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券 VO
 */
@Data
public class CouponVO {
    private Long id;
    private String couponName;
    private String couponType;
    private BigDecimal discountAmount;
    private BigDecimal minAmount;
    private String status;
    private LocalDateTime expireTime;
    private LocalDateTime createdTime;
}