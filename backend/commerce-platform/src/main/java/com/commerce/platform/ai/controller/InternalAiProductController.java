package com.commerce.platform.ai.controller;

import com.commerce.platform.ai.dto.InternalProductSearchResponse;
import com.commerce.platform.common.entity.Result;
import com.commerce.platform.product.dto.customer.ProductSearchRequest;
import com.commerce.platform.product.service.CustomerProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/ai/products")
public class InternalAiProductController {

    private final CustomerProductService customerProductService;

    @GetMapping("/search")
    public Result<InternalProductSearchResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "6") @Min(1) @Max(20) Integer pageSize) {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setPage(page);
        request.setSize(pageSize);

        var resultPage = customerProductService.listProducts(request);
        return Result.success(new InternalProductSearchResponse(
                resultPage.getContent(),
                resultPage.getTotalElements(),
                resultPage.getNumber() + 1,
                resultPage.getSize(),
                resultPage.getTotalPages()
        ));
    }
}