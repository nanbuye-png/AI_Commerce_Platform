package com.commerce.platform.cart.domain.repository;

import com.commerce.platform.cart.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CartItem 仓储
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 按购物车与 SKU 查询
     */
    Optional<CartItem> findByCartIdAndSkuId(Long cartId, Long skuId);

    /**
     * 物理删除购物车中的某个 SKU 条目。
     * 使用 @Modifying(clearAutomatically=true)：删除后立即清空 Hibernate 持久化上下文，
     * 避免后续 flush 时对 Cart 的 @OneToMany orphanRemoval 集合执行
     * "UPDATE cart_item SET cart_id=null"（cart_id 为 NOT NULL，会违反约束）而导致删除失败。
     *
     * @return 删除行数（0 表示该 SKU 不在购物车中）
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId AND ci.skuId = :skuId")
    int deleteByCartIdAndSkuId(@Param("cartId") Long cartId, @Param("skuId") Long skuId);
}