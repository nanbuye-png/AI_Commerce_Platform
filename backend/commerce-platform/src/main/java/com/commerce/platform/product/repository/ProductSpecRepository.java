package com.commerce.platform.product.repository;

import com.commerce.platform.product.entity.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品规格模板 Repository
 */
@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {
}