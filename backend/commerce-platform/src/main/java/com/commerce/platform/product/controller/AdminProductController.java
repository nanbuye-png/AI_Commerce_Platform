package com.commerce.platform.product.controller;

import com.commerce.platform.common.entity.PageResult;
import com.commerce.platform.common.entity.Result;
import com.commerce.platform.product.dto.admin.*;
import com.commerce.platform.product.service.ProductAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin 商品审核与生命周期管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductAuditService productAuditService;

    /**
     * 查看待审核商品列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<AdminProductListResponse>> listPendingProducts(AdminProductQueryRequest request) {
        Page<AdminProductListResponse> page = productAuditService.listPendingProducts(request);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取商品详情（Admin 可查看所有状态）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AdminProductDetailResponse> getProductDetail(@PathVariable Long id) {
        AdminProductDetailResponse response = productAuditService.getProductDetail(id);
        return Result.success(response);
    }

    /**
     * 审核通过：PENDING_REVIEW → ON_SHELF
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approveProduct(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ProductAuditRequest request) {
        Long reviewerId = getAdminId(authentication);
        productAuditService.approveProduct(id, reviewerId, request);
        return Result.success();
    }

    /**
     * 审核驳回：PENDING_REVIEW → REJECTED
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> rejectProduct(Authentication authentication,
                                      @PathVariable Long id,
                                      @Valid @RequestBody ProductAuditRequest request) {
        Long reviewerId = getAdminId(authentication);
        productAuditService.rejectProduct(id, reviewerId, request);
        return Result.success();
    }

    /**
     * 强制下架：ON_SHELF → OFF_SHELF
     */
    @PutMapping("/{id}/off-shelf")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> forceOffShelf(Authentication authentication,
                                      @PathVariable Long id,
                                      @Valid @RequestBody ProductAuditRequest request) {
        Long reviewerId = getAdminId(authentication);
        productAuditService.forceOffShelf(id, reviewerId, request);
        return Result.success();
    }

    /**
     * 恢复上架：OFF_SHELF → ON_SHELF
     */
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> restoreProduct(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ProductAuditRequest request) {
        Long reviewerId = getAdminId(authentication);
        productAuditService.restoreProduct(id, reviewerId, request);
        return Result.success();
    }

    private Long getAdminId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}