package com.commerce.platform.inventory.event.listener;

import com.commerce.platform.inventory.domain.entity.InventoryReservationEntity;
import com.commerce.platform.inventory.domain.enums.InvReservationStatus;
import com.commerce.platform.inventory.domain.repository.InventoryReservationEntityRepository;
import com.commerce.platform.inventory.service.InventoryApplicationService;
import com.commerce.platform.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单事件监听器
 * <p>
 * 监听 Order Domain 事件，执行库存操作。
 * 采用生产级幂等策略：重复 Event 不抛异常，直接跳过。
 * 不依赖 Order Entity，只依赖 Order Event。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final InventoryApplicationService inventoryApplicationService;
    private final InventoryReservationEntityRepository reservationRepository;

    /**
     * 订单创建时锁库存
     * <p>
     * 幂等策略：查询 Reservation 是否存在。
     * 存在 → 已处理，忽略重复事件，返回。
     * 不存在 → 执行锁库存并创建 Reservation。
     * </p>
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件：orderNo={}, items={}", event.getOrderNo(), event.getItems());

        for (var item : event.getItems()) {
            // 幂等检查：如果 Reservation 已存在，说明已经处理过，忽略
            if (reservationRepository.existsByOrderNoAndSkuId(event.getOrderNo(), item.getSkuId())) {
                log.info("库存已经锁定，忽略重复事件：orderNo={}, skuId={}",
                        event.getOrderNo(), item.getSkuId());
                continue;
            }

            // 锁定库存
            inventoryApplicationService.lockInventory(item.getSkuId(), item.getQuantity(), event.getOrderNo());

            // 创建预占记录
            InventoryReservationEntity reservation = InventoryReservationEntity.builder()
                    .orderNo(event.getOrderNo())
                    .skuId(item.getSkuId())
                    .quantity(item.getQuantity())
                    .status(InvReservationStatus.LOCKED)
                    .build();
            reservationRepository.save(reservation);
        }

        log.info("订单 {} 锁库存完成", event.getOrderNo());
    }
}