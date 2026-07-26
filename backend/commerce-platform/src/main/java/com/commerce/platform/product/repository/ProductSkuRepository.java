package com.commerce.platform.product.repository;

import com.commerce.platform.product.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品 SKU Repository
 */
@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
}