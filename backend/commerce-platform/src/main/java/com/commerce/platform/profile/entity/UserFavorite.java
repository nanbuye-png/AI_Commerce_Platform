package com.commerce.platform.profile.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 用户收藏夹实体
 */
@Entity
@Table(name = "user_favorite", indexes = {
    @Index(name = "idx_user_favorite_user_id", columnList = "user_id"),
    @Index(name = "uk_user_favorite_product", columnList = "user_id, product_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", length = 256)
    private String productName;

    @Column(name = "product_image", length = 512)
    private String productImage;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;
}