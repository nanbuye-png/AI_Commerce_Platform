package com.commerce.platform.product.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * 商品规格模板实体
 * 定义商品的可选规格维度（如颜色、尺寸、存储容量等）。
 */
@Entity
@Table(name = "product_spec", indexes = {
    @Index(name = "idx_product_spec_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class ProductSpec extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "spec_name", nullable = false, length = 64)
    private String specName;

    @Column(name = "spec_values", nullable = false, columnDefinition = "JSON")
    private String specValues;

    @Column(nullable = false)
    @Builder.Default
    private Integer sort = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}