package com.commerce.platform.inventory.stock.infrastructure.persistence;

import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 库存仓储实现
 * <p>
 * Infrastructure 层，实现 InventoryRepository 接口。
 * 负责 Domain Aggregate 与 JPA Entity 之间的转换。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryStockRepository {

    private final InventoryStockJpaRepository jpaRepository;

    @Override
    public Optional<InventoryStock> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).map(this::toDomain);
    }

    @Override
    public Optional<InventoryStock> findBySkuId(Long skuId) {
        return jpaRepository.findBySkuId(skuId).map(this::toDomain);
    }

    @Override
    public Optional<InventoryStock> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsBySkuId(Long skuId) {
        return jpaRepository.existsBySkuId(skuId);
    }

    @Override
    public List<InventoryStock> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryStock save(InventoryStock inventory) {
        InventoryStockEntity entity = toEntity(inventory);
        InventoryStockEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    /**
     * Domain Aggregate → JPA Entity
     */
    InventoryStockEntity toEntity(InventoryStock domain) {
        InventoryStockEntity entity = new InventoryStockEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setProductId(domain.getProductId());
        entity.setSkuId(domain.getSkuId());
        entity.setAvailableStock(domain.getAvailableQuantity());
        entity.setReservedStock(domain.getReservedQuantity());
        entity.setSoldStock(domain.getSoldQuantity());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    /**
     * JPA Entity → Domain Aggregate
     */
    InventoryStock toDomain(InventoryStockEntity entity) {
        return InventoryStock.restore(
                entity.getId(),
                entity.getProductId(),
                entity.getSkuId(),
                entity.getAvailableStock(),
                entity.getReservedStock(),
                entity.getSoldStock(),
                entity.getStatus()
        );
    }
}