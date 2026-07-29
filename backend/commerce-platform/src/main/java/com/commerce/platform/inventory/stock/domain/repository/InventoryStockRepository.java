package com.commerce.platform.inventory.stock.domain.repository;

import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;

import java.util.List;
import java.util.Optional;

/**
 * 库存仓储接口（Port）
 * <p>
 * 定义库存聚合的持久化操作接口，属于 Port（出站端口）。
 * 仅定义接口，不实现业务逻辑，实现由 Infrastructure 层完成。
 * Domain 层不依赖 Infrastructure。
 * </p>
 * <p>
 * 原名 InventoryRepository，为消除与旧架构 inventory.domain.repository.InventoryRepository
 * 的 Spring Bean 同名冲突，重命名为 InventoryStockRepository（Sprint 20 Step 1）。
 * Sprint 20 Step 2：新增 findBySkuId / findById / existsBySkuId / findAll，
 * 为后续 Service 迁移（InventoryApplicationService 等）提供等价查询能力。
 * </p>
 */
public interface InventoryStockRepository {

    /**
     * 根据商品ID查询库存
     *
     * @param productId 商品ID
     * @return 库存 Optional
     */
    Optional<InventoryStock> findByProductId(Long productId);

    /**
     * 根据 SKU ID 查询库存
     *
     * @param skuId SKU ID
     * @return 库存 Optional
     */
    Optional<InventoryStock> findBySkuId(Long skuId);

    /**
     * 根据 ID 查询库存
     *
     * @param id 库存 ID
     * @return 库存 Optional
     */
    Optional<InventoryStock> findById(Long id);

    /**
     * 检查 SKU ID 是否已存在
     *
     * @param skuId SKU ID
     * @return true 如果已存在
     */
    boolean existsBySkuId(Long skuId);

    /**
     * 查询所有库存
     *
     * @return 库存列表
     */
    List<InventoryStock> findAll();

    /**
     * 保存库存
     *
     * @param inventory 库存聚合
     * @return 保存后的库存
     */
    InventoryStock save(InventoryStock inventory);
}
