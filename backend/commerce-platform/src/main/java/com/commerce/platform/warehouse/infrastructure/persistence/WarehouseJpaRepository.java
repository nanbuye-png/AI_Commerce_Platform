package com.commerce.platform.warehouse.infrastructure.persistence;

import com.commerce.platform.warehouse.domain.valueobject.WarehouseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface WarehouseJpaRepository extends JpaRepository<WarehouseEntity, Long> {

    Optional<WarehouseEntity> findByCode(String code);

    Optional<WarehouseEntity> findFirstByStatus(WarehouseStatus status);
}