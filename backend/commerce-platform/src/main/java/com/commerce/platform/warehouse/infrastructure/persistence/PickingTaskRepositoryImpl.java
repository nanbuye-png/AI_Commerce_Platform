package com.commerce.platform.warehouse.infrastructure.persistence;

import com.commerce.platform.warehouse.domain.aggregate.PickingTask;
import com.commerce.platform.warehouse.domain.repository.PickingTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PickingTaskRepositoryImpl implements PickingTaskRepository {

    private final PickingTaskJpaRepository jpaRepository;

    @Override
    public PickingTask save(PickingTask task) {
        PickingTaskEntity entity = toEntity(task);
        PickingTaskEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PickingTask> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PickingTask> findByFulfillmentId(Long fulfillmentId) {
        return jpaRepository.findByFulfillmentId(fulfillmentId).map(this::toDomain);
    }

    PickingTaskEntity toEntity(PickingTask domain) {
        PickingTaskEntity entity = new PickingTaskEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setFulfillmentId(domain.getFulfillmentId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setStatus(domain.getStatus());
        entity.setCompletedAt(domain.getCompletedAt());
        return entity;
    }

    PickingTask toDomain(PickingTaskEntity entity) {
        return PickingTask.restore(
                entity.getId(), entity.getFulfillmentId(), entity.getWarehouseId(),
                entity.getStatus(), entity.getCreatedTime(), entity.getCompletedAt());
    }
}