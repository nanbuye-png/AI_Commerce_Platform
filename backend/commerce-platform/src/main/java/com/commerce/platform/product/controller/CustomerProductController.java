package com.commerce.platform.product.controller;

import com.commerce.platform.common.entity.PageResult;
import com.commerce.platform.common.entity.Result;
import com.commerce.platform.product.dto.customer.*;
import com.commerce.platform.product.service.CustomerProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端商品浏览 Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerProductController {

    private final CustomerProductService customerProductService;

    /**
     * 商品列表
     * GET /api/products
     */
    @GetMapping("/api/products")
    public Result<PageResult<ProductCardResponse>> listProducts(ProductSearchRequest request) {
        var page = customerProductService.listProducts(request);
        return Result.success(PageResult.of(page));
    }

    /**
     * 商品详情
     * GET /api/products/{id}
     */
    @GetMapping("/api/products/{id}")
    public Result<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
        ProductDetailResponse response = customerProductService.getProductDetail(id);
        return Result.success(response);
    }

    /**
     * 分类树
     * GET /api/categories/tree
     */
    @GetMapping("/api/categories/tree")
    public Result<List<CategoryTreeResponse>> getCategoryTree() {
        List<CategoryTreeResponse> tree = customerProductService.getCategoryTree();
        return Result.success(tree);
    }
}