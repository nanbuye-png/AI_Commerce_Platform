package com.commerce.platform.inventory.service;

import com.commerce.platform.inventory.event.InventoryDeductedEvent;
import com.commerce.platform.inventory.exception.InventoryNotFoundException;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存扣减 Application Service
 * <p>
 * Sprint 21 Step 2B: Migrated from Inventory Entity to InventoryStock Aggregate.
 * 支付成功后扣减库存：reserved-- → sold++。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryDeductApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public void deductInventory(Long skuId, Integer quantity, String orderNo) {
        InventoryStock stock = inventoryStockRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));

        // Sprint 21 Step 2B: 一次性确认（替代旧 for-loop deductStock）
        stock.confirm(quantity);
        inventoryStockRepository.save(stock);

        eventPublisher.publishEvent(new InventoryDeductedEvent(
                stock.getId(), orderNo, skuId, quantity));

        log.info("扣减库存成功：skuId={}, quantity={}, orderNo={}", skuId, quantity, orderNo);
    }
}