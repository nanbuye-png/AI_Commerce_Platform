package com.commerce.platform.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveCartItemRequest {
    @NotNull private Long skuId;
}