package com.commerce.platform.inventory.reservation.application.handler;

import com.commerce.platform.inventory.reservation.application.command.ConfirmStockCommand;
import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.event.StockConfirmedEvent;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 确认库存命令处理器
 * <p>
 * 职责链：接收 Command → Repository.findById() → Aggregate.confirm() → 保存 → 发布 Domain Event
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmStockHandler {

    private final StockReservationRepository stockReservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理确认库存命令
     *
     * @param command 确认库存命令
     * @return 已确认并持久化的库存预占
     * @throws IllegalArgumentException 如果预占不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public StockReservation handle(ConfirmStockCommand command) {
        log.info("开始确认库存预占: reservationId={}", command.getReservationId());

        // 1. 查询库存预占
        StockReservation reservation = stockReservationRepository.findById(command.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "库存预占不存在: reservationId=" + command.getReservationId()));

        // 2. 调用 Aggregate 领域方法确认预占
        reservation.confirm();

        // 3. 保存 Repository
        StockReservation savedReservation = stockReservationRepository.save(reservation);

        // 4. 发布 StockConfirmedEvent
        StockConfirmedEvent event = new StockConfirmedEvent(
                savedReservation.getId(),
                savedReservation.getOrderId(),
                savedReservation.getProductId(),
                savedReservation.getQuantity());
        eventPublisher.publishEvent(event);

        log.info("库存预占确认成功: reservationId={}, orderId={}",
                savedReservation.getId(), savedReservation.getOrderId());

        return savedReservation;
    }
}