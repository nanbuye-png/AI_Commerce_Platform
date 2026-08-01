package com.commerce.platform.product.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * 商品 SKU 实体
 * 库存量单位，表示具体销售规格。
 * 库存由 inventory 表统一管理，此表不存储库存数量。
 */
@Entity
@Table(name = "product_sku", indexes = {
    @Index(name = "idx_product_sku_product_id", columnList = "product_id"),
    @Index(name = "uk_sku_code", columnList = "sku_code", unique = true),
    @Index(name = "idx_sku_status", columnList = "product_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class ProductSku extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku_code", nullable = false, unique = true, length = 64)
    private String skuCode;

    @Column(name = "attributes_json", nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String attributesJson;

    @Column(nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal price;

    @Column(name = "original_price", precision = 12, scale = 2)
    private java.math.BigDecimal originalPrice;

    @Column(precision = 10, scale = 3)
    @Builder.Default
    private java.math.BigDecimal weight = java.math.BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "sales_count", nullable = false)
    @Builder.Default
    private Integer salesCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}