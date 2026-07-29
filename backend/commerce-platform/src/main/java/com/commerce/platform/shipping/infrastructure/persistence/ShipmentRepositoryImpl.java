package com.commerce.platform.shipping.infrastructure.persistence;

import com.commerce.platform.shipping.domain.aggregate.Shipment;
import com.commerce.platform.shipping.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShipmentRepositoryImpl implements ShipmentRepository {

    private final ShipmentJpaRepository jpaRepository;

    @Override
    public Shipment save(Shipment shipment) {
        return toDomain(jpaRepository.save(toEntity(shipment)));
    }

    @Override
    public Optional<Shipment> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Shipment> findByFulfillmentId(Long fulfillmentId) {
        return jpaRepository.findByFulfillmentId(fulfillmentId).map(this::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return jpaRepository.findByTrackingNumber(trackingNumber).map(this::toDomain);
    }

    ShipmentEntity toEntity(Shipment domain) {
        ShipmentEntity entity = new ShipmentEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setFulfillmentId(domain.getFulfillmentId());
        entity.setPackingTaskId(domain.getPackingTaskId());
        entity.setCarrier(domain.getCarrier());
        entity.setTrackingNumber(domain.getTrackingNumber());
        entity.setStatus(domain.getStatus());
        entity.setDeliveryStatus(domain.getDeliveryStatus());
        entity.setShippedAt(domain.getShippedAt());
        entity.setDeliveredAt(domain.getDeliveredAt());
        return entity;
    }

    Shipment toDomain(ShipmentEntity entity) {
        return Shipment.restore(
                entity.getId(), entity.getFulfillmentId(), entity.getPackingTaskId(),
                entity.getCarrier(), entity.getTrackingNumber(),
                entity.getStatus(), entity.getDeliveryStatus(),
                entity.getCreatedTime(), entity.getShippedAt(), entity.getDeliveredAt());
    }
}