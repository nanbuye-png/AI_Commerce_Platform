package com.commerce.platform.product.repository;

import com.commerce.platform.product.entity.Product;
import com.commerce.platform.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
     * Customer端：查询ON_SHELF商品，按名称模糊搜索
     */
    Page<Product> findByStatusAndProductNameContaining(
            ProductStatus status, String productName, Pageable pageable);

    /**
     * Customer端：查询ON_SHELF商品，按名称或分类名模糊搜索
     * 支持用户输入分类名（如"服装"、"电脑"）时也能命中对应分类下的商品。
     */
    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE p.status = :status
              AND (:keyword = ''
                   OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR p.categoryId IN (
                       SELECT c.id FROM Category c
                       WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR c.parentId IN (
                              SELECT parent.id FROM Category parent
                              WHERE LOWER(parent.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          )
                   ))
            """)
    Page<Product> findByStatusAndKeywordOrCategoryName(
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * Customer端：查询ON_SHELF商品，按名称模糊搜索 + 分类（含子分类）筛选
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:keyword = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND p.categoryId IN :categoryIds
            """)
    Page<Product> searchCustomerProductsByKeywordAndCategoryIds(
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            @Param("categoryIds") Collection<Long> categoryIds,
            Pageable pageable);

    /**
     * Customer端：查询ON_SHELF商品，按分类（含子分类）筛选
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND p.categoryId IN :categoryIds
            """)
    Page<Product> findByStatusAndCategoryIds(
            @Param("status") ProductStatus status,
            @Param("categoryIds") Collection<Long> categoryIds,
            Pageable pageable);

    /**
     * Customer端：查询所有ON_SHELF商品
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * Customer端：按价格区间查询（无分类过滤）
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:keyword = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
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
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Customer端：按价格区间 + 分类（含子分类）过滤查询
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:keyword = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND p.categoryId IN :categoryIds
              AND EXISTS (
                  SELECT sku.id FROM ProductSku sku
                  WHERE sku.product = p
                    AND sku.status = 'ACTIVE'
                    AND (:minPrice IS NULL OR sku.price >= :minPrice)
                    AND (:maxPrice IS NULL OR sku.price <= :maxPrice)
              )
            """)
    Page<Product> searchCustomerProductsByPriceAndCategoryIds(
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            @Param("categoryIds") Collection<Long> categoryIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Customer端：按名称/分类名 + 价格区间查询（无分类 ID 过滤）
     */
    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE p.status = :status
              AND (:keyword = ''
                   OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR p.categoryId IN (
                       SELECT c.id FROM Category c
                       WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR c.parentId IN (
                              SELECT parent.id FROM Category parent
                              WHERE LOWER(parent.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          )
                   ))
              AND EXISTS (
                  SELECT sku.id FROM ProductSku sku
                  WHERE sku.product = p
                    AND sku.status = 'ACTIVE'
                    AND (:minPrice IS NULL OR sku.price >= :minPrice)
                    AND (:maxPrice IS NULL OR sku.price <= :maxPrice)
              )
            """)
    Page<Product> searchCustomerProductsByKeywordOrCategoryAndPrice(
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}