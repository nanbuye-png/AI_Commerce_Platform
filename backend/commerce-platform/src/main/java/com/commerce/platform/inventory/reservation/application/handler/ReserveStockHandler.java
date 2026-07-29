package com.commerce.platform.inventory.reservation.application.handler;

import com.commerce.platform.inventory.reservation.application.command.ReserveStockCommand;
import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.event.StockReservedEvent;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.exception.InsufficientStockException;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import com.commerce.platform.inventory.stock.domain.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预占库存命令处理器（集成版）
 * <p>
 * 职责链：接收 Command → 查询 Inventory → 调用 Domain Service → 保存 Inventory & Reservation → 发布 Event
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveStockHandler {

    private final InventoryStockRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryReservationService inventoryReservationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理预占库存命令
     *
     * @param command 预占库存命令
     * @return 已创建并持久化的库存预占
     * @throws InsufficientStockException 库存不足
     */
    @Transactional(rollbackFor = Exception.class)
    public StockReservation handle(ReserveStockCommand command) {
        log.info("开始预占库存: orderId={}, productId={}, quantity={}",
                command.getOrderId(), command.getProductId(), command.getQuantity());

        // 1. 查询库存
        InventoryStock inventory = inventoryRepository.findByProductId(command.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "库存不存在: productId=" + command.getProductId()));

        // 2. 调用 Domain Service 校验并执行库存预占
        StockReservation reservation = inventoryReservationService.reserveStock(
                inventory, command.getQuantity(), command.getOrderId());

        // 3. 保存库存
        inventoryRepository.save(inventory);

        // 4. 保存预占记录
        StockReservation savedReservation = stockReservationRepository.save(reservation);

        // 5. 发布 StockReservedEvent
        StockReservedEvent event = new StockReservedEvent(
                savedReservation.getId(),
                savedReservation.getOrderId(),
                savedReservation.getProductId(),
                savedReservation.getQuantity());
        eventPublisher.publishEvent(event);

        log.info("库存预占成功: reservationId={}, orderId={}, productId={}, quantity={}, availableAfter={}",
                savedReservation.getId(), savedReservation.getOrderId(),
                savedReservation.getProductId(), savedReservation.getQuantity(),
                inventory.getAvailableQuantity());

        return savedReservation;
    }
}