package com.commerce.platform.product.repository;

import com.commerce.platform.product.entity.Product;
import com.commerce.platform.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.math.BigDecimal;

/**
 * 商品 SPU Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 按商品编码查询
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * 按商家ID分页查询
     */
    Page<Product> findByMerchantId(Long merchantId, Pageable pageable);

    /**
     * 按商家ID和状态分页查询
     */
    Page<Product> findByMerchantIdAndStatus(Long merchantId, ProductStatus status, Pageable pageable);

    /**
     * 按ID和状态查询（Customer端用）
     */
    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);

    /**
     * Customer端：查询ON_SHELF商品，支持名称模糊搜索和分类筛选
     */
    Page<Product> findByStatusAndProductNameContainingAndCategoryId(
            ProductStatus status, String productName, Long categoryId, Pageable pageable);

    /**
     * Customer端：查询ON_SHELF商品，按名称模糊搜索
     */
    Page<Product> findByStatusAndProductNameContaining(
            ProductStatus status, String productName, Pageable pageable);

    /**
     * Customer端：查询ON_SHELF商品，按分类筛选
     */
    Page<Product> findByStatusAndCategoryId(
            ProductStatus status, Long categoryId, Pageable pageable);

    /**
     * Customer端：查询所有ON_SHELF商品
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR p.categoryId = :categoryId)
              AND EXISTS (
                  SELECT sku.id FROM ProductSku sku
                  WHERE sku.product = p
                    AND sku.status = 'ACTIVE'
                    AND (:minPrice IS NULL OR sku.price >= :minPrice)
                    AND (:maxPrice IS NULL OR sku.price <= :maxPrice)
              )
            """)
    Page<Product> searchCustomerProductsByPrice(
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
