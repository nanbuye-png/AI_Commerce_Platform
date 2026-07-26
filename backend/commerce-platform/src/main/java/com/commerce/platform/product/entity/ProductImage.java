package com.commerce.platform.product.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.product.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * 商品图片实体
 * 隶属于 Product，支持多图，其中一张为首图。
 */
@Entity
@Table(name = "product_image", indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_product_cover", columnList = "product_id, is_cover")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private ImageType imageType;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(nullable = false)
    @Builder.Default
    private Integer sort = 0;

    @Column(name = "is_cover", nullable = false)
    @Builder.Default
    private Boolean isCover = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}