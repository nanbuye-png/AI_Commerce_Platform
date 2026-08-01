package com.commerce.platform.ai.dto;

import com.commerce.platform.product.dto.customer.ProductCardResponse;

import java.util.List;

public record InternalProductSearchResponse(
        List<ProductCardResponse> items,
        long total,
        int page,
        int pageSize,
        int totalPages
) {
}