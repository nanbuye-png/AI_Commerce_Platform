package com.commerce.platform.returns.infrastructure.persistence;

import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 退货仓储实现
 * <p>
 * Infrastructure 层实现，将 Domain 的 ReturnRequest 转换为 JPA Entity。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ReturnRequestRepositoryImpl implements ReturnRepository {

    private final ReturnRequestJpaRepository jpaRepository;

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        ReturnRequestEntity entity = toEntity(returnRequest);
        ReturnRequestEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ReturnRequest> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ReturnRequest> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnRequest> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ReturnRequestEntity toEntity(ReturnRequest r) {
        ReturnRequestEntity entity = new ReturnRequestEntity();
        entity.setId(r.getId());
        entity.setOrderId(r.getOrderId());
        entity.setUserId(r.getUserId());
        entity.setRefundId(r.getRefundId());
        entity.setReason(r.getReason());
        entity.setStatus(r.getStatus());
        entity.setCreatedAt(r.getCreatedAt());
        entity.setApprovedAt(r.getApprovedAt());
        entity.setCompletedAt(r.getCompletedAt());
        return entity;
    }

    private ReturnRequest toDomain(ReturnRequestEntity entity) {
        return ReturnRequest.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getUserId(),
                entity.getRefundId(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getApprovedAt(),
                entity.getCompletedAt()
        );
    }
}