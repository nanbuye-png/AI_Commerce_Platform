package com.commerce.platform.warehouse.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface PickingTaskJpaRepository extends JpaRepository<PickingTaskEntity, Long> {

    Optional<PickingTaskEntity> findByFulfillmentId(Long fulfillmentId);
}