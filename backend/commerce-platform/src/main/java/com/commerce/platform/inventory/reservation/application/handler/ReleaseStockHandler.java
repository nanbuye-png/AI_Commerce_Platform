package com.commerce.platform.inventory.reservation.application.handler;

import com.commerce.platform.inventory.reservation.application.command.ReleaseStockCommand;
import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.event.StockReleasedEvent;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import com.commerce.platform.inventory.stock.domain.service.InventoryReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 释放库存命令处理器（集成版）
 * <p>
 * 职责链：接收 Command → 查询 Reservation → 调用 InventoryReleaseService
 * → 保存 Inventory → 保存 Reservation → 发布 Event
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseStockHandler {

    private final StockReservationRepository stockReservationRepository;
    private final InventoryStockRepository inventoryRepository;
    private final InventoryReleaseService inventoryReleaseService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理释放库存命令
     *
     * @param command 释放库存命令
     * @return 已释放并持久化的库存预占
     * @throws IllegalArgumentException 如果预占不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public StockReservation handle(ReleaseStockCommand command) {
        log.info("开始释放库存预占: reservationId={}", command.getReservationId());

        // 1. 查询库存预占
        StockReservation reservation = stockReservationRepository.findById(command.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "库存预占不存在: reservationId=" + command.getReservationId()));

        // 2. 幂等检查：如果已 RELEASED，直接返回
        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            log.info("库存预占已释放，幂等忽略: reservationId={}", command.getReservationId());
            return reservation;
        }

        // 3. 调用 InventoryReleaseService 协调释放库存
        InventoryStock inventory = inventoryReleaseService.releaseStock(reservation);

        // 4. 保存库存
        inventoryRepository.save(inventory);

        // 5. 保存预占记录
        StockReservation savedReservation = stockReservationRepository.save(reservation);

        // 6. 发布 StockReleasedEvent
        StockReleasedEvent event = new StockReleasedEvent(
                savedReservation.getId(),
                savedReservation.getOrderId(),
                savedReservation.getProductId(),
                savedReservation.getQuantity());
        eventPublisher.publishEvent(event);

        log.info("库存预占释放成功: reservationId={}, orderId={}, availableAfter={}",
                savedReservation.getId(), savedReservation.getOrderId(),
                inventory.getAvailableQuantity());

        return savedReservation;
    }
}