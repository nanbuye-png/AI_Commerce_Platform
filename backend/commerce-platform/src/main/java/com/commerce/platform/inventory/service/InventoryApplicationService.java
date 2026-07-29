package com.commerce.platform.inventory.service;

import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.dto.request.CreateInventoryRequest;
import com.commerce.platform.inventory.dto.response.InventoryVO;
import com.commerce.platform.inventory.event.InventoryLockedEvent;
import com.commerce.platform.inventory.event.InventoryReleasedEvent;
import com.commerce.platform.inventory.exception.InventoryAlreadyExistsException;
import com.commerce.platform.inventory.exception.InventoryNotFoundException;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory Application Service
 * <p>
 * Sprint 21 Step 2B: Migrated from Inventory Entity + InventoryRepository
 * to InventoryStock Aggregate + InventoryStockRepository.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public InventoryVO createInventory(CreateInventoryRequest request) {
        if (inventoryStockRepository.existsBySkuId(request.getSkuId())) {
            throw new InventoryAlreadyExistsException(request.getSkuId());
        }

        InventoryStock stock = InventoryStock.create(request.getProductId(), request.getSkuId(), request.getInitialStock());
        inventoryStockRepository.save(stock);

        log.info("创建库存成功：productId={}, skuId={}, initialStock={}",
                request.getProductId(), request.getSkuId(), request.getInitialStock());

        return toInventoryVO(stock);
    }

    @Transactional(readOnly = true)
    public InventoryVO getInventory(Long skuId) {
        InventoryStock stock = inventoryStockRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));
        return toInventoryVO(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public InventoryVO lockInventory(Long skuId, Integer quantity, String orderNo) {
        InventoryStock stock = inventoryStockRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));

        // Sprint 21 Step 2B: 一次性预占（替代旧 for-loop lockStock）
        stock.reserve(quantity);
        inventoryStockRepository.save(stock);

        eventPublisher.publishEvent(new InventoryLockedEvent(
                stock.getId(), stock.getProductId(), skuId, quantity, orderNo));

        log.info("预占库存成功：skuId={}, quantity={}, orderNo={}", skuId, quantity, orderNo);
        return toInventoryVO(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public InventoryVO releaseInventory(Long skuId, Integer quantity, String orderNo) {
        InventoryStock stock = inventoryStockRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));

        // Sprint 21 Step 2B: 一次性释放（替代旧 for-loop releaseStock）
        stock.release(quantity);
        inventoryStockRepository.save(stock);

        eventPublisher.publishEvent(new InventoryReleasedEvent(
                stock.getId(), skuId, quantity, orderNo));

        log.info("释放库存成功：skuId={}, quantity={}, orderNo={}", skuId, quantity, orderNo);
        return toInventoryVO(stock);
    }

    private InventoryVO toInventoryVO(InventoryStock stock) {
        InventoryVO vo = new InventoryVO();
        vo.setId(stock.getId());
        vo.setProductId(stock.getProductId());
        vo.setSkuId(stock.getSkuId());
        vo.setAvailableStock(stock.getAvailableQuantity());
        vo.setLockedStock(stock.getReservedQuantity());  // DTO 保持 lockedStock 字段名
        vo.setSoldStock(stock.getSoldQuantity());
        vo.setStatus(stock.getStatus() != null ? stock.getStatus().name() : InventoryStatus.AVAILABLE.name());
        return vo;
    }
}