package com.commerce.platform.warehouse.domain.aggregate;

import com.commerce.platform.warehouse.domain.exception.InvalidWarehouseStatusException;
import com.commerce.platform.warehouse.domain.valueobject.WarehouseStatus;

import java.time.LocalDateTime;

/**
 * 仓库聚合根
 * <p>
 * 代表一个实体仓库，负责维护仓库自身状态。
 * 状态变更必须通过领域方法完成。
 * </p>
 */
public class Warehouse {

    private Long id;
    private String code;
    private String name;
    private String address;
    private WarehouseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 创建新仓库
     *
     * @param code    仓库编码
     * @param name    仓库名称
     * @param address 仓库地址
     * @return 新建的仓库（状态为 INACTIVE）
     */
    public static Warehouse create(String code, String name, String address) {
        Warehouse warehouse = new Warehouse();
        warehouse.code = code;
        warehouse.name = name;
        warehouse.address = address;
        warehouse.status = WarehouseStatus.INACTIVE;
        warehouse.createdAt = LocalDateTime.now();
        warehouse.updatedAt = LocalDateTime.now();
        return warehouse;
    }

    /**
     * 从持久化恢复仓库
     */
    public static Warehouse restore(Long id, String code, String name, String address,
                                    WarehouseStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Warehouse warehouse = new Warehouse();
        warehouse.id = id;
        warehouse.code = code;
        warehouse.name = name;
        warehouse.address = address;
        warehouse.status = status;
        warehouse.createdAt = createdAt;
        warehouse.updatedAt = updatedAt;
        return warehouse;
    }

    // ============================================
    // 领域行为 —— 状态流转
    // ============================================

    /**
     * 激活仓库
     * INACTIVE → ACTIVE
     */
    public void activate() {
        transitionTo(WarehouseStatus.ACTIVE, "activate");
    }

    /**
     * 禁用仓库
     * ACTIVE → INACTIVE
     */
    public void disable() {
        transitionTo(WarehouseStatus.INACTIVE, "disable");
    }

    /**
     * 设置仓库为维护状态
     * ACTIVE → MAINTENANCE
     */
    public void maintenance() {
        transitionTo(WarehouseStatus.MAINTENANCE, "maintenance");
    }

    // ============================================
    // 内部方法
    // ============================================

    private void transitionTo(WarehouseStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidWarehouseStatusException(
                    this.id, this.status.name(), target.name());
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ============================================
    // Getters
    // ============================================

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public WarehouseStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * 判断仓库是否可用于履约
     */
    public boolean isAvailable() {
        return status.isAvailable();
    }
}