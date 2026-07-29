package com.commerce.platform.warehouse.domain.repository;

import com.commerce.platform.warehouse.domain.aggregate.PackingTask;

import java.util.Optional;

/**
 * 打包任务仓储接口
 */
public interface PackingTaskRepository {

    PackingTask save(PackingTask task);

    Optional<PackingTask> findById(Long id);

    Optional<PackingTask> findByPickingTaskId(Long pickingTaskId);
}