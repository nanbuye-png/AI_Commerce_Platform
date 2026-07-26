package com.commerce.platform.inventory.service;

import com.commerce.platform.common.entity.PageResult;
import com.commerce.platform.inventory.dto.merchant.*;
import org.springframework.data.domain.Page;

/**
 * 库存服务接口
 * <p>
 * Inventory Domain 的核心服务，提供库存查询、调整、入库、流水查询等功能。
 * 所有库存变更必须经过此 Service，禁止 Controller 直接操作 Repository。
 * </p>
 */
public interface InventoryService {

    /**
     * 分页查询商家库存列表
     *
     * @param merchantId 商家 ID
     * @param query      查询条件
     * @return 分页结果
     */
    Page<InventoryListResponse> listInventory(Long merchantId, InventoryQueryRequest query);

    /**
     * 查询库存详情
     *
     * @param merchantId  商家 ID
     * @param inventoryId 库存记录 ID
     * @return 库存详情
     */
    InventoryDetailResponse getInventoryDetail(Long merchantId, Long inventoryId);

    /**
     * 调整库存（增加/减少）
     *
     * @param merchantId  商家 ID
     * @param inventoryId 库存记录 ID
     * @param request     调整请求
     */
    void adjustInventory(Long merchantId, Long inventoryId, InventoryAdjustRequest request);

    /**
     * 入库操作
     *
     * @param merchantId  商家 ID
     * @param inventoryId 库存记录 ID
     * @param request     入库请求（复用 AdjustRequest，仅支持 INCREASE）
     */
    void inboundInventory(Long merchantId, Long inventoryId, InventoryAdjustRequest request);

    /**
     * 查询库存流水
     *
     * @param merchantId  商家 ID
     * @param inventoryId 库存记录 ID
     * @param page        页码
     * @param pageSize    每页条数
     * @return 分页流水
     */
    Page<InventoryMovementResponse> listInventoryMovements(Long merchantId, Long inventoryId, int page, int pageSize);
}