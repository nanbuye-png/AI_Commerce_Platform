package com.commerce.platform.inventory.event.listener;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
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
 * <p>
 * Sprint 20 Step 4C: Reservation 从 InventoryReservationEntity 迁移到 StockReservation。
 * 锁定库存仍通过 InventoryApplicationService（保留 old Inventory 模型兼容）。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final InventoryApplicationService inventoryApplicationService;
    private final StockReservationRepository stockReservationRepository;

    /**
     * 订单创建时锁库存
     * <p>
     * 幂等策略：查询 StockReservation 是否存在。
     * 存在 → 已处理，忽略重复事件，返回。
     * 不存在 → 执行锁库存并创建 StockReservation。
     * </p>
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件：orderId={}, orderNo={}, items={}", event.getOrderId(), event.getOrderNo(), event.getItems());

        for (var item : event.getItems()) {
            Long productId = item.getProductId();
            // 幂等检查：如果 StockReservation 已存在，说明已经处理过，忽略
            if (productId != null && stockReservationRepository.existsByOrderIdAndProductId(
                    event.getOrderId(), productId)) {
                log.info("库存已经锁定，忽略重复事件：orderId={}, productId={}",
                        event.getOrderId(), productId);
                continue;
            }
            // 向后兼容：如果 productId 为 null（旧 Event 格式），使用 skuId 的回退检查
            if (productId == null) {
                log.info("OrderCreatedEvent 缺少 productId（旧格式），使用 skuId={} 通过 InventoryApplicationService 锁定",
                        item.getSkuId());
            }

            // 锁定库存（仍使用旧的 InventoryApplicationService，操作 locked_stock 列）
            inventoryApplicationService.lockInventory(item.getSkuId(), item.getQuantity(), event.getOrderNo());

            // 创建 StockReservation 预占记录（新架构，写入 stock_reservation 表）
            Long resolvedProductId = productId != null ? productId : item.getSkuId(); // fallback
            StockReservation reservation = StockReservation.create(
                    event.getOrderId(), resolvedProductId, item.getQuantity());
            stockReservationRepository.save(reservation);
        }

        log.info("订单 {} 锁库存完成", event.getOrderNo());
    }
}