package com.commerce.platform.inventory.stock.infrastructure.persistence;

import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 库存仓储实现
 * <p>
 * Infrastructure 层，实现 InventoryRepository 接口。
 * 负责 Domain Aggregate 与 JPA Entity 之间的转换。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryStockJpaRepository jpaRepository;

    @Override
    public Optional<InventoryStock> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).map(this::toDomain);
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
                entity.getSoldStock()
        );
    }
}