package com.commerce.platform.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddCartItemRequest {
    @NotNull private Long skuId;
    @NotNull private Long productId;
    @NotNull private String productName;
    private String productImage;
    @NotNull private BigDecimal price;
    @Min(1) @NotNull private Integer quantity;
}