package com.commerce.platform.product.service;

import com.commerce.platform.product.dto.customer.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * C端商品浏览服务接口
 */
public interface CustomerProductService {

    /**
     * 商品列表（仅 ON_SHELF）
     */
    Page<ProductCardResponse> listProducts(ProductSearchRequest request);

    /**
     * 商品详情（仅 ON_SHELF）
     */
    ProductDetailResponse getProductDetail(Long productId);

    /**
     * 分类树
     */
    List<CategoryTreeResponse> getCategoryTree();
}