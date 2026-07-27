package com.commerce.platform.inventory.service;

import com.commerce.platform.inventory.domain.entity.Inventory;
import com.commerce.platform.inventory.domain.repository.InventoryRepository;
import com.commerce.platform.inventory.event.InventoryDeductedEvent;
import com.commerce.platform.inventory.exception.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存扣减 Application Service
 * <p>
 * 支付成功后扣减库存。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryDeductApplicationService {

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 扣减库存
     * <p>
     * 状态变化：LOCKED → DEDUCTED
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductInventory(Long skuId, Integer quantity, String orderNo) {
        Inventory inventory = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new InventoryNotFoundException(skuId));

        for (int i = 0; i < quantity; i++) {
            inventory.deductStock();
        }

        inventoryRepository.save(inventory);

        eventPublisher.publishEvent(new InventoryDeductedEvent(
                inventory.getId(), orderNo, skuId, quantity));

        log.info("扣减库存成功：skuId={}, quantity={}, orderNo={}", skuId, quantity, orderNo);
    }
}