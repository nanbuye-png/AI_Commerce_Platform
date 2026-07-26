package com.commerce.platform.product.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 商品分类实体
 * 多级树形分类结构，支持无限层级。根节点的 parentId = 0。
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "parent_id", nullable = false)
    @Builder.Default
    private Long parentId = 0L;

    @Column(name = "category_name", nullable = false, length = 64)
    private String categoryName;

    @Column(nullable = false)
    @Builder.Default
    private Integer sort = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}