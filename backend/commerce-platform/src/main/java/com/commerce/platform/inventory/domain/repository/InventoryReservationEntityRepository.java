package com.commerce.platform.inventory.domain.repository;

import com.commerce.platform.inventory.domain.entity.InventoryReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存预占记录 Repository
 */
@Repository
public interface InventoryReservationEntityRepository extends JpaRepository<InventoryReservationEntity, Long> {
    Optional<InventoryReservationEntity> findByOrderNoAndSkuId(String orderNo, Long skuId);
    boolean existsByOrderNoAndSkuId(String orderNo, Long skuId);
}