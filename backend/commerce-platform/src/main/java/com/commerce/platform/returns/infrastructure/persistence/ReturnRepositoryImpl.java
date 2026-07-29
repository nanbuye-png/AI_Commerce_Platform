package com.commerce.platform.returns.infrastructure.persistence;

import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReturnRepositoryImpl implements ReturnRepository {
    private final ReturnJpaRepository jpaRepository;

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        ReturnEntity entity = toEntity(returnRequest);
        ReturnEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ReturnRequest> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ReturnRequest> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ReturnRequest> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private ReturnEntity toEntity(ReturnRequest request) {
        ReturnEntity entity = new ReturnEntity();
        entity.setId(request.getId());
        entity.setOrderId(request.getOrderId());
        entity.setUserId(request.getUserId());
        entity.setRefundId(request.getRefundId());
        entity.setReason(request.getReason());
        entity.setStatus(request.getStatus());
        entity.setCreatedAt(request.getCreatedAt());
        entity.setApprovedAt(request.getApprovedAt());
        entity.setCompletedAt(request.getCompletedAt());
        return entity;
    }

    private ReturnRequest toDomain(ReturnEntity entity) {
        return ReturnRequest.restore(
                entity.getId(), entity.getOrderId(), entity.getUserId(),
                entity.getRefundId(), entity.getReason(), entity.getStatus(),
                entity.getCreatedAt(), entity.getApprovedAt(), entity.getCompletedAt());
    }
}