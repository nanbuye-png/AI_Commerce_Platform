package com.commerce.platform.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单条目 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

    private Long id;

    private Long skuId;

    private Long productId;

    private String productName;

    private String skuName;

    private String skuCode;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String image;

    private Integer quantity;

    private BigDecimal subtotal;
}