package com.commerce.platform.warehouse.domain.valueobject;

/**
 * 仓库状态枚举
 * <p>
 * 定义仓库的可用状态。
 * 状态迁移必须遵守合法路径。
 * </p>
 *
 * <pre>
 * 合法迁移：
 * INACTIVE → ACTIVE
 * ACTIVE   → MAINTENANCE
 * ACTIVE   → INACTIVE
 * MAINTENANCE → ACTIVE
 * </pre>
 */
public enum WarehouseStatus {

    /** 可用 */
    ACTIVE,
    /** 不可用 */
    INACTIVE,
    /** 维护中 */
    MAINTENANCE;

    /**
     * 判断从当前状态是否可以迁移到目标状态
     *
     * @param target 目标状态
     * @return true 如果迁移合法
     */
    public boolean canTransitionTo(WarehouseStatus target) {
        return switch (this) {
            case INACTIVE -> target == ACTIVE;
            case ACTIVE -> target == MAINTENANCE || target == INACTIVE;
            case MAINTENANCE -> target == ACTIVE;
        };
    }

    /**
     * 判断仓库是否可用（ACTIVE状态表示可以接收履约任务）
     *
     * @return true 如果仓库可用
     */
    public boolean isAvailable() {
        return this == ACTIVE;
    }
}