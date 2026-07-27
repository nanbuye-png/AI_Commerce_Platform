package com.commerce.platform.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull private Long skuId;
    @NotNull private Integer quantity;
}