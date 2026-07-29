package com.commerce.platform.warehouse.infrastructure.persistence;

import com.commerce.platform.warehouse.domain.aggregate.PackingTask;
import com.commerce.platform.warehouse.domain.repository.PackingTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PackingTaskRepositoryImpl implements PackingTaskRepository {

    private final PackingTaskJpaRepository jpaRepository;

    @Override
    public PackingTask save(PackingTask task) {
        PackingTaskEntity entity = toEntity(task);
        PackingTaskEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PackingTask> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PackingTask> findByPickingTaskId(Long pickingTaskId) {
        return jpaRepository.findByPickingTaskId(pickingTaskId).map(this::toDomain);
    }

    PackingTaskEntity toEntity(PackingTask domain) {
        PackingTaskEntity entity = new PackingTaskEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setFulfillmentId(domain.getFulfillmentId());
        entity.setPickingTaskId(domain.getPickingTaskId());
        entity.setStatus(domain.getStatus());
        entity.setPackedAt(domain.getPackedAt());
        return entity;
    }

    PackingTask toDomain(PackingTaskEntity entity) {
        return PackingTask.restore(
                entity.getId(), entity.getFulfillmentId(), entity.getPickingTaskId(),
                entity.getStatus(), entity.getCreatedTime(), entity.getPackedAt());
    }
}