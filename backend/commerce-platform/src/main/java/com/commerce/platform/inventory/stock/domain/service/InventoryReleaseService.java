package com.commerce.platform.inventory.stock.domain.service;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 库存释放领域服务
 * <p>
 * 协调 InventoryStock 和 StockReservation 两个 Aggregate 完成库存释放。
 * 幂等保护：如果 StockReservation 已经 RELEASED，则忽略不重复处理。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReleaseService {

    private final InventoryRepository inventoryRepository;

    /**
     * 释放库存
     * <p>
     * 流程：
     * 1. 幂等检查：如已 RELEASED，返回 null（不重复释放库存）
     * 2. 查询 InventoryStock
     * 3. 执行 inventory.release(quantity)
     * 4. 执行 reservation.release()
     * 5. 返回修改后的 InventoryStock
     * </p>
     *
     * @param reservation 库存预占聚合
     * @return 修改后的 InventoryStock，幂等时返回 null
     * @throws IllegalArgumentException 库存预占不存在
     */
    public InventoryStock releaseStock(StockReservation reservation) {
        // 幂等检查：如果已 RELEASED，直接忽略
        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            log.info("库存预占已释放，幂等忽略: reservationId={}, orderId={}",
                    reservation.getId(), reservation.getOrderId());
            return null;
        }

        // 查询 InventoryStock
        InventoryStock inventory = inventoryRepository.findByProductId(reservation.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "库存不存在: productId=" + reservation.getProductId()));

        // 执行库存释放（含校验）
        inventory.release(reservation.getQuantity());

        // 执行预占释放（含状态校验）
        reservation.release();

        log.info("库存释放成功: reservationId={}, productId={}, quantity={}, availableAfter={}",
                reservation.getId(), reservation.getProductId(),
                reservation.getQuantity(), inventory.getAvailableQuantity());

        return inventory;
    }
}