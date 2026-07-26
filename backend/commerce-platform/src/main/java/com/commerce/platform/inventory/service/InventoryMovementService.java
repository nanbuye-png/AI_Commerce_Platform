package com.commerce.platform.inventory.service;

import com.commerce.platform.inventory.dto.movement.InventoryMovementDetailResponse;
import com.commerce.platform.inventory.dto.movement.InventoryMovementQueryRequest;
import com.commerce.platform.inventory.dto.movement.InventoryMovementResponse;
import org.springframework.data.domain.Page;

/**
 * 库存流水查询服务接口
 * <p>
 * 提供库存流水审计查询能力，支持 Merchant 和 Admin 两种角色。
 * Merchant 仅查看本店流水，Admin 查看全平台。
 * </p>
 */
public interface InventoryMovementService {

    /**
     * 分页查询库存流水（商家端 - 按 Inventory ID 过滤）
     */
    Page<InventoryMovementResponse> listMovements(Long merchantId, InventoryMovementQueryRequest query);

    /**
     * 分页查询库存流水（管理员端 - 全平台）
     */
    Page<InventoryMovementResponse> listAllMovements(InventoryMovementQueryRequest query);

    /**
     * 查询流水详情
     */
    InventoryMovementDetailResponse getMovementDetail(Long movementId);

    /**
     * 预留：导出流水（后续 Sprint 实现）
     */
    void exportMovements(InventoryMovementQueryRequest query);
}