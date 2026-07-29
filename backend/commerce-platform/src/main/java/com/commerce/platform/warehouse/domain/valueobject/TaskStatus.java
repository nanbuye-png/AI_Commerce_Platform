package com.commerce.platform.warehouse.domain.valueobject;

/**
 * 仓库任务状态枚举
 * <p>
 * 定义拣货任务（PickingTask）和打包任务（PackingTask）的生命周期状态。
 * </p>
 *
 * <pre>
 * 合法迁移：
 * CREATED → PROCESSING
 * PROCESSING → COMPLETED / FAILED
 * 任意非终态 → CANCELLED
 * </pre>
 */
public enum TaskStatus {

    /** 已创建 */
    CREATED,
    /** 处理中 */
    PROCESSING,
    /** 已完成 */
    COMPLETED,
    /** 已失败 */
    FAILED,
    /** 已取消 */
    CANCELLED;

    /**
     * 判断从当前状态是否可以迁移到目标状态
     *
     * @param target 目标状态
     * @return true 如果迁移合法
     */
    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case CREATED -> target == PROCESSING || target == CANCELLED;
            case PROCESSING -> target == COMPLETED || target == FAILED || target == CANCELLED;
            case COMPLETED -> false;
            case FAILED -> false;
            case CANCELLED -> false;
        };
    }
}