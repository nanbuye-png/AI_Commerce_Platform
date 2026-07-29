package com.commerce.platform.shipping.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface ShipmentJpaRepository extends JpaRepository<ShipmentEntity, Long> {
    Optional<ShipmentEntity> findByFulfillmentId(Long fulfillmentId);
    Optional<ShipmentEntity> findByTrackingNumber(String trackingNumber);
}