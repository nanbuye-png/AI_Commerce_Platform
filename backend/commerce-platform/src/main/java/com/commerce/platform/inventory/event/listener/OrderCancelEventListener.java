package com.commerce.platform.inventory.event.listener;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;
import com.commerce.platform.inventory.service.InventoryApplicationService;
import com.commerce.platform.order.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单取消事件监听器
 * <p>
 * 监听 Order Domain 的订单取消事件，释放已锁定的库存。
 * 根据 Reservation 状态进行幂等处理：
 * - RESERVED → 释放库存 + 更新 Reservation 状态
 * - RELEASED → 已释放，忽略
 * - CONFIRMED → 不允许释放，记录日志
 * </p>
 * <p>
 * Sprint 20 Step 4C: Reservation 从 InventoryReservationEntity 迁移到 StockReservation。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelEventListener {

    private final InventoryApplicationService inventoryApplicationService;
    private final StockReservationRepository stockReservationRepository;

    /**
     * 订单取消时释放库存
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("收到订单取消事件：orderId={}, orderNo={}", event.getOrderId(), event.getOrderNo());

        if (event.getItems() == null) {
            log.info("订单取消事件无 items 信息，跳过库存释放：orderNo={}", event.getOrderNo());
            return;
        }

        for (var item : event.getItems()) {
            Long productId = item.getProductId();
            if (productId == null) {
                log.warn("OrderCancelledEvent 缺少 productId，跳过：orderId={}, skuId={}",
                        event.getOrderId(), item.getSkuId());
                continue;
            }

            // 查询 StockReservation
            var reservationOpt = stockReservationRepository.findByOrderIdAndProductId(
                    event.getOrderId(), productId);

            if (reservationOpt.isEmpty()) {
                log.warn("未找到 StockReservation 预占记录，跳过：orderId={}, productId={}",
                        event.getOrderId(), productId);
                continue;
            }

            StockReservation reservation = reservationOpt.get();

            if (reservation.getStatus() == ReservationStatus.RELEASED) {
                log.info("库存已释放，忽略重复取消事件：orderId={}, productId={}",
                        event.getOrderId(), productId);
                continue;
            }

            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                log.warn("库存已确认，不允许释放：orderId={}, productId={}",
                        event.getOrderId(), productId);
                continue;
            }

            // 释放库存
            inventoryApplicationService.releaseInventory(
                    item.getSkuId(), item.getQuantity(), event.getOrderNo());

            // 更新 StockReservation 状态
            reservation.release();
            stockReservationRepository.save(reservation);

            log.info("释放库存成功：orderId={}, productId={}, skuId={}",
                    event.getOrderId(), productId, item.getSkuId());
        }
    }
}