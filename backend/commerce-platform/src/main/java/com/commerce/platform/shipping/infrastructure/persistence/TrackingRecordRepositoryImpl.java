package com.commerce.platform.shipping.infrastructure.persistence;

import com.commerce.platform.shipping.domain.aggregate.TrackingRecord;
import com.commerce.platform.shipping.domain.repository.TrackingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrackingRecordRepositoryImpl implements TrackingRecordRepository {

    private final TrackingRecordJpaRepository jpaRepository;

    @Override
    public TrackingRecord save(TrackingRecord record) {
        TrackingRecordEntity entity = toEntity(record);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<TrackingRecord> findByShipmentId(Long shipmentId) {
        return jpaRepository.findByShipmentIdOrderByOccurredAtDesc(shipmentId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<TrackingRecord> findLatestByShipmentId(Long shipmentId) {
        return jpaRepository.findFirstByShipmentIdOrderByOccurredAtDesc(shipmentId).map(this::toDomain);
    }

    TrackingRecordEntity toEntity(TrackingRecord domain) {
        TrackingRecordEntity entity = new TrackingRecordEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setShipmentId(domain.getShipmentId());
        entity.setLocation(domain.getLocation());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setOccurredAt(domain.getOccurredAt());
        return entity;
    }

    TrackingRecord toDomain(TrackingRecordEntity entity) {
        return TrackingRecord.restore(
                entity.getId(), entity.getShipmentId(),
                entity.getLocation(), entity.getDescription(),
                entity.getStatus(), entity.getOccurredAt());
    }
}