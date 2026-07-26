package com.commerce.platform.inventory.domain.repository;

import com.commerce.platform.inventory.domain.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 库存 Repository
 * <p>
 * Inventory Domain 聚合根的 Repository 接口。
 * 仅继承 JpaRepository，复杂查询通过 Service 层组织。
 * </p>
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}