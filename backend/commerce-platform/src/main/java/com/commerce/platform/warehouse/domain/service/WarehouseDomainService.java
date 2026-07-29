package com.commerce.platform.warehouse.domain.service;

import com.commerce.platform.warehouse.domain.aggregate.PickingTask;
import com.commerce.platform.warehouse.domain.aggregate.Warehouse;
import com.commerce.platform.warehouse.domain.repository.PickingTaskRepository;
import com.commerce.platform.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 仓库领域服务
 * <p>
 * 负责根据履约单创建拣货任务，协调 Warehouse 与 Task 聚合。
 * 不直接修改 Task 状态，状态变化由 Aggregate 自身维护。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class WarehouseDomainService {

    private final WarehouseRepository warehouseRepository;
    private final PickingTaskRepository pickingTaskRepository;

    /**
     * 创建拣货任务
     * <p>
     * 1. 查找可用仓库
     * 2. 校验仓库可用
     * 3. 创建拣货任务
     * </p>
     *
     * @param fulfillmentId 履约单ID
     * @return 创建的拣货任务
     * @throws IllegalStateException 如果无可用仓库
     */
    public PickingTask createPickingTask(Long fulfillmentId) {
        // 查找可用仓库
        Warehouse warehouse = warehouseRepository.findActiveWarehouse()
                .orElseThrow(() -> new IllegalStateException("无可用仓库"));

        // 校验仓库状态
        if (!warehouse.isAvailable()) {
            throw new IllegalStateException("仓库不可用: warehouseId=" + warehouse.getId());
        }

        // 创建拣货任务（由 PickingTask 自身聚合维护初始状态）
        return PickingTask.create(fulfillmentId, warehouse.getId());
    }

    /**
     * 查找可用仓库
     *
     * @return 仓库 Optional
     */
    public Warehouse findActiveWarehouse() {
        return warehouseRepository.findActiveWarehouse().orElse(null);
    }
}