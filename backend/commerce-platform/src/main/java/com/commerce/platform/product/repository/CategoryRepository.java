package com.commerce.platform.product.repository;

import com.commerce.platform.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品分类 Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 按父分类ID查询子分类列表（按sort排序）
     */
    List<Category> findByParentIdOrderBySortAsc(Long parentId);
}
