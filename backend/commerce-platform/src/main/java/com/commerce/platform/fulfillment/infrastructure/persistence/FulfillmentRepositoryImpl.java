package com.commerce.platform.fulfillment.infrastructure.persistence;

import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.repository.FulfillmentRepository;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import com.commerce.platform.fulfillment.domain.valueobject.ShipmentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 履约单仓储实现
 * <p>
 * Infrastructure 层，实现 FulfillmentRepository 接口。
 * 负责 Domain Aggregate 与 JPA Entity 之间的转换。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class FulfillmentRepositoryImpl implements FulfillmentRepository {

    private final FulfillmentJpaRepository jpaRepository;

    @Override
    public Fulfillment save(Fulfillment fulfillment) {
        FulfillmentEntity entity = toEntity(fulfillment);
        FulfillmentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Fulfillment> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Fulfillment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }

    @Override
    public List<Fulfillment> findByStatus(FulfillmentStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Domain Aggregate → JPA Entity
     */
    FulfillmentEntity toEntity(Fulfillment domain) {
        FulfillmentEntity entity = new FulfillmentEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setOrderId(domain.getOrderId());
        entity.setMerchantId(domain.getMerchantId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setStatus(domain.getStatus());

        // 转换 ShipmentInfo
        ShipmentInfo shipmentInfo = domain.getShipmentInfo();
        if (shipmentInfo != null) {
            entity.setCarrier(shipmentInfo.getCarrier());
            entity.setCarrierCode(shipmentInfo.getCarrierCode());
            entity.setTrackingNumber(shipmentInfo.getTrackingNumber());
            entity.setShippingAddress(shipmentInfo.getShippingAddress());
            entity.setEstimatedArrival(shipmentInfo.getEstimatedArrival());
        }

        return entity;
    }

    /**
     * JPA Entity → Domain Aggregate
     */
    Fulfillment toDomain(FulfillmentEntity entity) {
        ShipmentInfo shipmentInfo = null;
        if (entity.getCarrier() != null && entity.getTrackingNumber() != null) {
            shipmentInfo = new ShipmentInfo(
                    entity.getCarrier(),
                    entity.getCarrierCode(),
                    entity.getTrackingNumber(),
                    entity.getShippingAddress(),
                    entity.getEstimatedArrival()
            );
        }

        return Fulfillment.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getMerchantId(),
                entity.getWarehouseId(),
                entity.getStatus(),
                shipmentInfo,
                entity.getCreatedTime(),
                entity.getUpdatedTime()
        );
    }
}