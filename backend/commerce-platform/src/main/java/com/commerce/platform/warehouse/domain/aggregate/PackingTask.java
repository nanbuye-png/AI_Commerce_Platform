package com.commerce.platform.warehouse.domain.aggregate;

import com.commerce.platform.warehouse.domain.exception.InvalidTaskStatusException;
import com.commerce.platform.warehouse.domain.valueobject.TaskStatus;

import java.time.LocalDateTime;

/**
 * 打包任务聚合根
 * <p>
 * 表示仓库履约流程中拣货完成后的打包任务。
 * 状态变更必须通过领域方法完成。
 * </p>
 */
public class PackingTask {

    private Long id;
    private Long fulfillmentId;
    private Long pickingTaskId;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime packedAt;

    /**
     * 创建打包任务
     *
     * @param fulfillmentId 履约单ID
     * @param pickingTaskId 关联的拣货任务ID
     * @return 新建的打包任务（状态为 CREATED）
     */
    public static PackingTask create(Long fulfillmentId, Long pickingTaskId) {
        PackingTask task = new PackingTask();
        task.fulfillmentId = fulfillmentId;
        task.pickingTaskId = pickingTaskId;
        task.status = TaskStatus.CREATED;
        task.createdAt = LocalDateTime.now();
        return task;
    }

    /**
     * 从持久化恢复打包任务
     */
    public static PackingTask restore(Long id, Long fulfillmentId, Long pickingTaskId,
                                      TaskStatus status, LocalDateTime createdAt, LocalDateTime packedAt) {
        PackingTask task = new PackingTask();
        task.id = id;
        task.fulfillmentId = fulfillmentId;
        task.pickingTaskId = pickingTaskId;
        task.status = status;
        task.createdAt = createdAt;
        task.packedAt = packedAt;
        return task;
    }

    // ============================================
    // 领域行为
    // ============================================

    /**
     * 开始打包
     * CREATED → PACKING
     */
    public void startPacking() {
        transitionTo(TaskStatus.PROCESSING, "startPacking");
    }

    /**
     * 完成打包
     * PROCESSING → COMPLETED
     */
    public void completePacking() {
        transitionTo(TaskStatus.COMPLETED, "completePacking");
        this.packedAt = LocalDateTime.now();
    }

    /**
     * 打包失败
     * PROCESSING → FAILED
     */
    public void failPacking() {
        transitionTo(TaskStatus.FAILED, "failPacking");
    }

    // ============================================
    // 内部方法
    // ============================================

    private void transitionTo(TaskStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidTaskStatusException(
                    this.id, "PackingTask", this.status.name(), target.name());
        }
        this.status = target;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ============================================
    // Getters
    // ============================================

    public Long getId() { return id; }
    public Long getFulfillmentId() { return fulfillmentId; }
    public Long getPickingTaskId() { return pickingTaskId; }
    public TaskStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPackedAt() { return packedAt; }
}