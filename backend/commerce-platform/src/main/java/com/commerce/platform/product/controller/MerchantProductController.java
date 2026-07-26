package com.commerce.platform.product.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.product.dto.merchant.*;
import com.commerce.platform.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 商家端商品管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
public class MerchantProductController {

    private final ProductService productService;

    /**
     * 创建商品
     */
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Long> createProduct(Authentication authentication,
                                      @Valid @RequestBody CreateProductRequest request) {
        Long merchantId = getMerchantId(authentication);
        Long productId = productService.createProduct(merchantId, request);
        return Result.success(productId);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> updateProduct(Authentication authentication,
                                      @PathVariable Long id,
                                      @Valid @RequestBody UpdateProductRequest request) {
        Long merchantId = getMerchantId(authentication);
        productService.updateProduct(merchantId, id, request);
        return Result.success();
    }

    /**
     * 删除商品（软删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> deleteProduct(Authentication authentication,
                                      @PathVariable Long id) {
        Long merchantId = getMerchantId(authentication);
        productService.deleteProduct(merchantId, id);
        return Result.success();
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<ProductDetailResponse> getProductDetail(Authentication authentication,
                                                          @PathVariable Long id) {
        Long merchantId = getMerchantId(authentication);
        ProductDetailResponse response = productService.getProductDetail(merchantId, id);
        return Result.success(response);
    }

    /**
     * 查询我的商品列表
     */
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Page<ProductListResponse>> listMyProducts(Authentication authentication,
                                                            ProductQueryRequest query) {
        Long merchantId = getMerchantId(authentication);
        Page<ProductListResponse> page = productService.listMyProducts(merchantId, query);
        return Result.success(page);
    }

    /**
     * 从 Authentication 中获取商家 ID
     * Authentication principal 中存储的是 userId (Long)
     */
    private Long getMerchantId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}