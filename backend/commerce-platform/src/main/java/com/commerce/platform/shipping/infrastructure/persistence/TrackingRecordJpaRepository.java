package com.commerce.platform.shipping.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface TrackingRecordJpaRepository extends JpaRepository<TrackingRecordEntity, Long> {
    List<TrackingRecordEntity> findByShipmentIdOrderByOccurredAtDesc(Long shipmentId);
    Optional<TrackingRecordEntity> findFirstByShipmentIdOrderByOccurredAtDesc(Long shipmentId);
}