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
 */
@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    /**
     * 根据预占编号查询
     */
    Optional<InventoryReservation> findByReservationNo(String reservationNo);
}
