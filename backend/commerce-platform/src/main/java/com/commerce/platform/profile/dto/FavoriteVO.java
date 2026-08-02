package com.commerce.platform.profile.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏夹 VO
 */
@Data
public class FavoriteVO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private LocalDateTime createdTime;
}