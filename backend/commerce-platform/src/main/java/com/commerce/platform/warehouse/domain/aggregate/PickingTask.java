package com.commerce.platform.warehouse.domain.aggregate;

import com.commerce.platform.warehouse.domain.exception.InvalidTaskStatusException;
import com.commerce.platform.warehouse.domain.valueobject.TaskStatus;

import java.time.LocalDateTime;

/**
 * 拣货任务聚合根
 * <p>
 * 表示仓库履约流程中的一次拣货任务。
 * 状态变更必须通过领域方法完成。
 * </p>
 */
public class PickingTask {

    private Long id;
    private Long fulfillmentId;
    private Long warehouseId;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    /**
     * 创建拣货任务
     *
     * @param fulfillmentId 履约单ID
     * @param warehouseId   仓库ID
     * @return 新建的拣货任务（状态为 CREATED）
     */
    public static PickingTask create(Long fulfillmentId, Long warehouseId) {
        PickingTask task = new PickingTask();
        task.fulfillmentId = fulfillmentId;
        task.warehouseId = warehouseId;
        task.status = TaskStatus.CREATED;
        task.createdAt = LocalDateTime.now();
        return task;
    }

    /**
     * 从持久化恢复拣货任务
     */
    public static PickingTask restore(Long id, Long fulfillmentId, Long warehouseId,
                                      TaskStatus status, LocalDateTime createdAt, LocalDateTime completedAt) {
        PickingTask task = new PickingTask();
        task.id = id;
        task.fulfillmentId = fulfillmentId;
        task.warehouseId = warehouseId;
        task.status = status;
        task.createdAt = createdAt;
        task.completedAt = completedAt;
        return task;
    }

    // ============================================
    // 领域行为
    // ============================================

    /**
     * 开始拣货
     * CREATED → PROCESSING
     */
    public void startPicking() {
        transitionTo(TaskStatus.PROCESSING, "startPicking");
    }

    /**
     * 完成拣货
     * PROCESSING → COMPLETED
     */
    public void completePicking() {
        transitionTo(TaskStatus.COMPLETED, "completePicking");
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 拣货失败
     * PROCESSING → FAILED
     */
    public void failPicking() {
        transitionTo(TaskStatus.FAILED, "failPicking");
    }

    /**
     * 取消拣货
     * CREATED / PROCESSING → CANCELLED
     */
    public void cancel() {
        transitionTo(TaskStatus.CANCELLED, "cancel");
    }

    // ============================================
    // 内部方法
    // ============================================

    private void transitionTo(TaskStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidTaskStatusException(
                    this.id, "PickingTask", this.status.name(), target.name());
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
    public Long getWarehouseId() { return warehouseId; }
    public TaskStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}