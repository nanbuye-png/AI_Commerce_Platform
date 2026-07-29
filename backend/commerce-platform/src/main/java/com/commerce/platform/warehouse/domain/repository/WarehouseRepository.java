package com.commerce.platform.warehouse.domain.repository;

import com.commerce.platform.warehouse.domain.aggregate.Warehouse;

import java.util.Optional;

/**
 * 仓库仓储接口
 */
public interface WarehouseRepository {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(Long id);

    /**
     * 查询可用的仓库（ACTIVE状态）
     */
    Optional<Warehouse> findActiveWarehouse();
}