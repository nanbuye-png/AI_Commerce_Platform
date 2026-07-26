package com.commerce.platform.product.repository;

import com.commerce.platform.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品图片 Repository
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}