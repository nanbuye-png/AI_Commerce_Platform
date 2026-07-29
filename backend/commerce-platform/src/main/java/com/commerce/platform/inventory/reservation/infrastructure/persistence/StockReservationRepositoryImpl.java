package com.commerce.platform.inventory.reservation.infrastructure.persistence;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 库存预占仓储实现
 * <p>
 * Infrastructure 层，实现 StockReservationRepository 接口。
 * 负责 Domain Aggregate 与 JPA Entity 之间的转换。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class StockReservationRepositoryImpl implements StockReservationRepository {

    private final StockReservationJpaRepository jpaRepository;

    @Override
    public StockReservation save(StockReservation reservation) {
        StockReservationEntity entity = toEntity(reservation);
        StockReservationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<StockReservation> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<StockReservation> findByOrderIdAndProductId(Long orderId, Long productId) {
        return jpaRepository.findByOrderIdAndProductId(orderId, productId).map(this::toDomain);
    }

    @Override
    public boolean existsByOrderIdAndProductId(Long orderId, Long productId) {
        return jpaRepository.existsByOrderIdAndProductId(orderId, productId);
    }

    /**
     * Domain Aggregate → JPA Entity
     */
    StockReservationEntity toEntity(StockReservation domain) {
        StockReservationEntity entity = new StockReservationEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setOrderId(domain.getOrderId());
        entity.setProductId(domain.getProductId());
        entity.setQuantity(domain.getQuantity());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setConfirmedAt(domain.getConfirmedAt());
        entity.setReleasedAt(domain.getReleasedAt());
        return entity;
    }

    /**
     * JPA Entity → Domain Aggregate
     */
    StockReservation toDomain(StockReservationEntity entity) {
        return StockReservation.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getConfirmedAt(),
                entity.getReleasedAt()
        );
    }
}