package com.commerce.platform.inventory.domain.repository;

import com.commerce.platform.inventory.domain.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存预占 Repository
 * <p>
 * 仅继承 JpaRepository，复杂查询通过 Service 层组织。
 * </p>
 *
 * @deprecated Sprint 20 Step 2 — 旧架构，指向已标记 @Deprecated 的 InventoryReservation Entity。
 * 请使用新架构：{@link com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository}
 * （映射 stock_reservation 表）。此 Repository 将在 Phase 3 删除。
 */
@Deprecated
@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    /**
     * 根据预占编号查询
     */
    Optional<InventoryReservation> findByReservationNo(String reservationNo);
}
