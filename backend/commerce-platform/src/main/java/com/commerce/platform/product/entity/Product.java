package com.commerce.platform.product.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品 SPU 实体
 * Product Domain 的聚合根。
 */
@Entity
@Table(name = "product", indexes = {
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_store_id", columnList = "store_id"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_merchant_status", columnList = "merchant_id, status"),
    @Index(name = "idx_status_created", columnList = "status, created_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Product extends BaseEntity {

    /**
     * 商品业务编码，全局唯一，创建后不可修改
     */
    @Column(name = "product_code", nullable = false, unique = true, length = 64, updatable = false)
    private String productCode;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "product_name", nullable = false, length = 256)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 64)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "sales_count", nullable = false)
    @Builder.Default
    private Integer salesCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * 乐观锁版本号，防止并发编辑导致数据覆盖
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    /**
     * 商品图片列表
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sort ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    /**
     * 商品规格模板列表
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sort ASC")
    @Builder.Default
    private List<ProductSpec> specs = new ArrayList<>();

    /**
     * 商品 SKU 列表
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductSku> skus = new ArrayList<>();
}