package com.commerce.platform.inventory.service;

import com.commerce.platform.inventory.domain.entity.Inventory;
import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.domain.repository.InventoryRepository;
import com.commerce.platform.inventory.dto.request.CreateInventoryRequest;
import com.commerce.platform.inventory.dto.response.InventoryVO;
import com.commerce.platform.inventory.event.InventoryLockedEvent;
import com.commerce.platform.inventory.event.InventoryReleasedEvent;
import com.commerce.platform.inventory.exception.InventoryAlreadyExistsException;
import com.commerce.platform.inventory.exception.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory Application Service
 * <p>
 * 负责：
 * <ul>
 *   <li>事务管理</li>
 *   <li>Repository 调用</li>
 *   <li>Entity 行为调用</li>
 *   <li>Domain Event 发布</li>
 * </ul>
 * </p>
 *
 * 禁止直接修改 availableStock / lockedStock / soldStock / status，
 * 必须调用 Entity 提供的领域方法。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryApplicationService {

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建库存
     *
     * @param request 创建请求
     * @return 库存 VO
     * @throws InventoryAlreadyExistsException 当 SKU ID 已存在时
     */
    @Transactional(rollbackFor = Exception.class)
    public InventoryVO createInventory(CreateInventoryRequest request) {
        // 检查 SKU ID 是否已存在
        if (inventoryRepository.existsBySkuId(request.getSkuId())) {
            throw new InventoryAlreadyExistsException(request.getSkuId());
        }

        // 创建 Inventory Entity
        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .skuId(request.getSkuId())
                .availableStock(request.getInitialStock())
                .lockedStock(0)
                .soldStock(0)
                .status(InventoryStatus.AVAILABLE)
                .build();

        inventoryRepository.save(inventory);

        log.info("创建库存成功：productId={}, skuId={}, initialStock={}",
                request.getProductId(), request.getSkuId(), request.getInitialStock());

        return toInventoryVO(inventory);
    }

    /**
     * 根据 SKU ID 查询库存
     *
     * @param skuId SKU ID
     * @return 库存 VO
     * @throws InventoryNotFoundException 当库存记录不存在时
     */
    @Transactional(readOnly = true)
    public InventoryVO getInventory(Long skuId) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));
        return toInventoryVO(inventory);
    }

    /**
     * 锁定库存
     * <p>
     * 状态变化：AVAILABLE → LOCKED
     * </p>
     *
     * @param skuId    SKU ID
     * @param quantity 锁定数量
     * @param orderNo  订单号
     * @return 库存 VO
     * @throws InventoryNotFoundException     当库存记录不存在时
     * @throws com.commerce.platform.inventory.exception.InsufficientInventoryException 当库存不足时
     */
    @Transactional(rollbackFor = Exception.class)
    public InventoryVO lockInventory(Long skuId, Integer quantity, String orderNo) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));

        // 多次锁定：每次锁定 quantity 个
        for (int i = 0; i < quantity; i++) {
            inventory.lockStock();
        }

        inventoryRepository.save(inventory);

        // 发布 Domain Event
        eventPublisher.publishEvent(new InventoryLockedEvent(
                inventory.getId(), inventory.getProductId(), skuId, quantity, orderNo));

        log.info("锁定库存成功：skuId={}, quantity={}, orderNo={}", skuId, quantity, orderNo);

        return toInventoryVO(inventory);
    }

    /**
     * 释放库存
     * <p>
     * 状态变化：LOCKED → RELEASED
     * </p>
     *
     * @param skuId    SKU ID
     * @param quantity 释放数量
     * @param orderNo  订单号
     * @return 库存 VO
     * @throws InventoryNotFoundException 当库存记录不存在时
     */
    @Transactional(rollbackFor = Exception.class)
    public InventoryVO releaseInventory(Long skuId, Integer quantity, String orderNo) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));

        // 多次释放：每次释放 quantity 个
        for (int i = 0; i < quantity; i++) {
            inventory.releaseStock();
        }

        inventoryRepository.save(inventory);

        // 发布 Domain Event
        eventPublisher.publishEvent(new InventoryReleasedEvent(
                inventory.getId(), skuId, quantity, orderNo));

        log.info("释放库存成功：skuId={}, quantity={}, orderNo={}", skuId, quantity, orderNo);

        return toInventoryVO(inventory);
    }

    // ========== 私有方法 ==========

    private InventoryVO toInventoryVO(Inventory inventory) {
        InventoryVO vo = new InventoryVO();
        vo.setId(inventory.getId());
        vo.setProductId(inventory.getProductId());
        vo.setSkuId(inventory.getSkuId());
        vo.setAvailableStock(inventory.getAvailableStock());
        vo.setLockedStock(inventory.getLockedStock());
        vo.setSoldStock(inventory.getSoldStock());
        vo.setStatus(inventory.getStatus().name());
        return vo;
    }
}