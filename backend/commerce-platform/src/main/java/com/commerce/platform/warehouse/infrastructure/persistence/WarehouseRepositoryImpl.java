package com.commerce.platform.warehouse.infrastructure.persistence;

import com.commerce.platform.warehouse.domain.aggregate.Warehouse;
import com.commerce.platform.warehouse.domain.repository.WarehouseRepository;
import com.commerce.platform.warehouse.domain.valueobject.WarehouseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final WarehouseJpaRepository jpaRepository;

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseEntity entity = toEntity(warehouse);
        WarehouseEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Warehouse> findActiveWarehouse() {
        return jpaRepository.findFirstByStatus(WarehouseStatus.ACTIVE).map(this::toDomain);
    }

    WarehouseEntity toEntity(Warehouse domain) {
        WarehouseEntity entity = new WarehouseEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setAddress(domain.getAddress());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    Warehouse toDomain(WarehouseEntity entity) {
        return Warehouse.restore(
                entity.getId(), entity.getCode(), entity.getName(),
                entity.getAddress(), entity.getStatus(),
                entity.getCreatedTime(), entity.getUpdatedTime());
    }
}