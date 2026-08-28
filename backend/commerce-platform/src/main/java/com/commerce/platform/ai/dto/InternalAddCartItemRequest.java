package com.commerce.platform.ai.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InternalAddCartItemRequest(
        @NotNull Long userId,
        @NotNull Long skuId,
        @NotNull @Min(1) Integer quantity
) {
}
