package com.commerce.platform.inventory.stock.domain.repository;

import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;

import java.util.Optional;

/**
 * 库存仓储接口（Port）
 * <p>
 * 定义库存聚合的持久化操作接口，属于 Port（出站端口）。
 * 仅定义接口，不实现业务逻辑，实现由 Infrastructure 层完成。
 * Domain 层不依赖 Infrastructure。
 * </p>
 */
public interface InventoryRepository {

    /**
     * 根据商品ID查询库存
     *
     * @param productId 商品ID
     * @return 库存 Optional
     */
    Optional<InventoryStock> findByProductId(Long productId);

    /**
     * 保存库存
     *
     * @param inventory 库存聚合
     * @return 保存后的库存
     */
    InventoryStock save(InventoryStock inventory);
}