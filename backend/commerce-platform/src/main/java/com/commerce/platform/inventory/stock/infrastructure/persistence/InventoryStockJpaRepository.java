package com.commerce.platform.inventory.stock.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存 JPA 数据访问接口
 * <p>
 * Infrastructure 层，继承 Spring Data JPA 提供的基础 CRUD。
 * 由 InventoryRepositoryImpl 适配调用，非 Domain 层直接使用。
 * </p>
 */
@Repository
interface InventoryStockJpaRepository extends JpaRepository<InventoryStockEntity, Long> {

    /**
     * 根据商品ID查询库存
     *
     * @param productId 商品ID
     * @return 库存记录
     */
    Optional<InventoryStockEntity> findByProductId(Long productId);
}