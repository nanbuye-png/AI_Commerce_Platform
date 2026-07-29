package com.commerce.platform.refund.infrastructure.persistence;

import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 退款仓储实现
 * <p>
 * Infrastructure 层实现，将 Domain 的 Refund 转换为 JPA Entity。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundJpaRepository jpaRepository;

    @Override
    public Refund save(Refund refund) {
        RefundEntity entity = toEntity(refund);
        RefundEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Refund> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private RefundEntity toEntity(Refund refund) {
        RefundEntity entity = new RefundEntity();
        entity.setId(refund.getId());
        entity.setOrderId(refund.getOrderId());
        entity.setUserId(refund.getUserId());
        entity.setAmount(refund.getAmount());
        entity.setReason(refund.getReason());
        entity.setStatus(refund.getStatus());
        entity.setCreatedAt(refund.getCreatedAt());
        entity.setCompletedAt(refund.getCompletedAt());
        return entity;
    }

    private Refund toDomain(RefundEntity entity) {
        return Refund.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getUserId(),
                entity.getAmount(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }
}