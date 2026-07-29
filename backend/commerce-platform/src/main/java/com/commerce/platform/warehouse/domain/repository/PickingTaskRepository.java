package com.commerce.platform.warehouse.domain.repository;

import com.commerce.platform.warehouse.domain.aggregate.PickingTask;

import java.util.Optional;

/**
 * 拣货任务仓储接口
 */
public interface PickingTaskRepository {

    PickingTask save(PickingTask task);

    Optional<PickingTask> findById(Long id);

    Optional<PickingTask> findByFulfillmentId(Long fulfillmentId);
}