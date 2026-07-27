package com.commerce.platform.inventory.event.listener;

import com.commerce.platform.inventory.domain.entity.InventoryReservationEntity;
import com.commerce.platform.inventory.domain.enums.InvReservationStatus;
import com.commerce.platform.inventory.domain.repository.InventoryReservationEntityRepository;
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
 * - LOCKED → 释放库存 + 更新 Reservation 状态
 * - RELEASED → 已释放，忽略
 * - DEDUCTED → 不允许释放，记录日志
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelEventListener {

    private final InventoryApplicationService inventoryApplicationService;
    private final InventoryReservationEntityRepository reservationRepository;

    /**
     * 订单取消时释放库存
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("收到订单取消事件：orderNo={}", event.getOrderNo());

        if (event.getItems() == null) {
            log.info("订单取消事件无 items 信息，跳过库存释放：orderNo={}", event.getOrderNo());
            return;
        }

        for (var item : event.getItems()) {
            // 查询 Reservation
            var reservationOpt = reservationRepository.findByOrderNoAndSkuId(
                    event.getOrderNo(), item.getSkuId());

            if (reservationOpt.isEmpty()) {
                log.warn("未找到预占记录，跳过：orderNo={}, skuId={}",
                        event.getOrderNo(), item.getSkuId());
                continue;
            }

            InventoryReservationEntity reservation = reservationOpt.get();

            switch (reservation.getStatus()) {
                case LOCKED:
                    // 释放库存
                    inventoryApplicationService.releaseInventory(
                            item.getSkuId(), item.getQuantity(), event.getOrderNo());
                    // 更新 Reservation 状态
                    reservation.release();
                    reservationRepository.save(reservation);
                    log.info("释放库存成功：orderNo={}, skuId={}", event.getOrderNo(), item.getSkuId());
                    break;

                case RELEASED:
                    // 已释放，幂等忽略
                    log.info("库存已释放，忽略重复取消事件：orderNo={}, skuId={}",
                            event.getOrderNo(), item.getSkuId());
                    break;

                case DEDUCTED:
                    // 已扣减的不允许释放
                    log.warn("库存已扣减，不允许释放：orderNo={}, skuId={}",
                            event.getOrderNo(), item.getSkuId());
                    break;
            }
        }
    }
}