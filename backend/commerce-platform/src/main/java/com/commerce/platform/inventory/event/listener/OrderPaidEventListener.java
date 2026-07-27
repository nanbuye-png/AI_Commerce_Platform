package com.commerce.platform.inventory.event.listener;

import com.commerce.platform.inventory.domain.entity.InventoryReservationEntity;
import com.commerce.platform.inventory.domain.enums.InvReservationStatus;
import com.commerce.platform.inventory.domain.repository.InventoryReservationEntityRepository;
import com.commerce.platform.inventory.event.InventoryDeductedEvent;
import com.commerce.platform.inventory.service.InventoryDeductApplicationService;
import com.commerce.platform.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单支付事件监听器（属于 Inventory Domain）
 * <p>
 * 监听 OrderPaidEvent，执行库存正式扣减：
 * 1. 查询 InventoryReservation（LOCKED）
 * 2. 调用 InventoryDeductApplicationService.deductInventory() 扣减库存
 * 3. 标记 Reservation 为 DEDUCTED
 * </p>
 *
 * 不依赖 Order Entity，通过 Event 解耦。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {

    private final InventoryDeductApplicationService deductApplicationService;
    private final InventoryReservationEntityRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("收到订单支付事件，开始扣减库存：orderNo={}", event.getOrderNo());

        // 查询所有 LOCKED 的 Reservation 记录
        // 注意：这里简化处理，实际应按 orderNo 查询所有 SKU 明细
        var reservations = reservationRepository.findAll().stream()
                .filter(r -> r.getOrderNo().equals(event.getOrderNo())
                        && r.getStatus() == InvReservationStatus.LOCKED)
                .toList();

        if (reservations.isEmpty()) {
            log.warn("未找到 LOCKED 的库存预占记录：orderNo={}", event.getOrderNo());
            return;
        }

        for (InventoryReservationEntity reservation : reservations) {
            try {
                // 幂等处理：检查是否已扣减
                if (reservation.getStatus() != InvReservationStatus.LOCKED) {
                    log.warn("库存预占状态不是 LOCKED，跳过：orderNo={}, skuId={}, status={}",
                            event.getOrderNo(), reservation.getSkuId(), reservation.getStatus());
                    continue;
                }

                // 扣减库存
                deductApplicationService.deductInventory(
                        reservation.getSkuId(), reservation.getQuantity(), event.getOrderNo());

                // 标记 Reservation 为 DEDUCTED
                reservation.deduct();
                reservationRepository.save(reservation);

                log.info("库存扣减成功：orderNo={}, skuId={}, quantity={}",
                        event.getOrderNo(), reservation.getSkuId(), reservation.getQuantity());

            } catch (Exception e) {
                log.error("库存扣减失败：orderNo={}, skuId={}, error={}",
                        event.getOrderNo(), reservation.getSkuId(), e.getMessage(), e);
            }
        }
    }
}