package com.commerce.platform.product.service;

import com.commerce.platform.product.dto.merchant.*;
import org.springframework.data.domain.Page;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 创建商品
     *
     * @param merchantId 商家ID
     * @param request    创建请求
     * @return 商品ID
     */
    Long createProduct(Long merchantId, CreateProductRequest request);

    /**
     * 更新商品
     *
     * @param merchantId 商家ID
     * @param productId  商品ID
     * @param request    更新请求
     */
    void updateProduct(Long merchantId, Long productId, UpdateProductRequest request);

    /**
     * 删除商品（软删除）
     *
     * @param merchantId 商家ID
     * @param productId  商品ID
     */
    void deleteProduct(Long merchantId, Long productId);

    /**
     * 获取商品详情
     *
     * @param merchantId 商家ID
     * @param productId  商品ID
     * @return 商品详情
     */
    ProductDetailResponse getProductDetail(Long merchantId, Long productId);

    /**
     * 查询商家自己的商品列表
     *
     * @param merchantId 商家ID
     * @param query      查询参数
     * @return 分页商品列表
     */
    Page<ProductListResponse> listMyProducts(Long merchantId, ProductQueryRequest query);
}