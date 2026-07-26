package com.commerce.platform.product.service;

import com.commerce.platform.product.dto.admin.*;
import org.springframework.data.domain.Page;

/**
 * 商品审核与生命周期管理服务接口
 */
public interface ProductAuditService {

    /**
     * 查看待审核商品列表（PENDING_REVIEW）
     */
    Page<AdminProductListResponse> listPendingProducts(AdminProductQueryRequest request);

    /**
     * 获取商品详情（Admin可查看所有状态）
     */
    AdminProductDetailResponse getProductDetail(Long productId);

    /**
     * 审核通过：PENDING_REVIEW → ON_SHELF
     */
    void approveProduct(Long productId, Long reviewerId, ProductAuditRequest request);

    /**
     * 审核驳回：PENDING_REVIEW → REJECTED
     */
    void rejectProduct(Long productId, Long reviewerId, ProductAuditRequest request);

    /**
     * 强制下架：ON_SHELF → OFF_SHELF
     */
    void forceOffShelf(Long productId, Long reviewerId, ProductAuditRequest request);

    /**
     * 恢复上架：OFF_SHELF → ON_SHELF
     */
    void restoreProduct(Long productId, Long reviewerId, ProductAuditRequest request);
}