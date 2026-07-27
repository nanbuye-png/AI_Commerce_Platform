package com.commerce.platform.inventory.domain.repository;

import com.commerce.platform.inventory.domain.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存 Repository
 * <p>
 * Inventory Domain 聚合根的 Repository 接口。
 * 提供根据 SKU ID 和商品 ID + SKU ID 的查询方法。
 * </p>
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * 根据 SKU ID 查询库存
     *
     * @param skuId SKU ID
     * @return 库存记录
     */
    Optional<Inventory> findBySkuId(Long skuId);

    /**
     * 根据商品 ID 和 SKU ID 查询库存
     *
     * @param productId 商品 ID
     * @param skuId     SKU ID
     * @return 库存记录
     */
    Optional<Inventory> findByProductIdAndSkuId(Long productId, Long skuId);

    /**
     * 检查 SKU ID 是否已存在
     *
     * @param skuId SKU ID
     * @return true 如果已存在
     */
    boolean existsBySkuId(Long skuId);
}
