package com.commerce.platform.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收藏/浏览历史 操作请求
 */
@Data
public class FavoriteToggleRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal price;
}