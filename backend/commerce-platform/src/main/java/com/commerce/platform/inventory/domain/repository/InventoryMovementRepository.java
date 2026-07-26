package com.commerce.platform.inventory.domain.repository;

import com.commerce.platform.inventory.domain.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 库存流水 Repository
 * <p>
 * 仅继承 JpaRepository，复杂查询通过 Service 层组织。
 * 流水表采用 Append-Only 模式，不提供 UPDATE/DELETE 操作。
 * </p>
 */
@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
}