package com.commerce.platform.inventory.integration;

import com.commerce.platform.inventory.domain.entity.Inventory;
import com.commerce.platform.inventory.domain.entity.InventoryReservationEntity;
import com.commerce.platform.inventory.domain.enums.InvReservationStatus;
import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.domain.repository.InventoryRepository;
import com.commerce.platform.inventory.domain.repository.InventoryReservationEntityRepository;
import com.commerce.platform.inventory.event.listener.OrderCancelEventListener;
import com.commerce.platform.inventory.event.listener.OrderEventListener;
import com.commerce.platform.inventory.service.InventoryApplicationService;
import com.commerce.platform.order.event.OrderCancelledEvent;
import com.commerce.platform.order.event.OrderCreatedEvent;
import com.commerce.platform.order.event.OrderCreatedEvent.OrderItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Order → Inventory 集成测试
 * <p>
 * 测试订单创建锁库存、取消释放库存、重复事件的幂等处理。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class OrderInventoryIntegrationTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryReservationEntityRepository reservationRepository;

    private InventoryApplicationService inventoryApplicationService;
    private OrderEventListener orderEventListener;
    private OrderCancelEventListener orderCancelEventListener;

    @BeforeEach
    void setUp() {
        inventoryApplicationService = new InventoryApplicationService(inventoryRepository, mock(org.springframework.context.ApplicationEventPublisher.class));
        orderEventListener = new OrderEventListener(inventoryApplicationService, reservationRepository);
        orderCancelEventListener = new OrderCancelEventListener(inventoryApplicationService, reservationRepository);
    }

    private Inventory createInventory(int availableStock) {
        Inventory inventory = Inventory.builder()
                .productId(1L)
                .skuId(1001L)
                .availableStock(availableStock)
                .lockedStock(0)
                .soldStock(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        inventory.setId(1L);
        return inventory;
    }

    // ============================================
    // 测试1：订单创建锁库存
    // ============================================

    @Test
    @DisplayName("订单创建锁库存：availableStock=100，锁10 → available=90, locked=10")
    void shouldLockInventoryOnOrderCreated() {
        Inventory inventory = createInventory(100);

        when(inventoryRepository.findBySkuId(1001L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.existsByOrderNoAndSkuId("ORDER001", 1001L)).thenReturn(false);

        OrderCreatedEvent event = new OrderCreatedEvent("ORDER001", 1L,
                List.of(new OrderItemDto(1001L, 10)));

        orderEventListener.onOrderCreated(event);

        assertEquals(90, inventory.getAvailableStock());
        assertEquals(10, inventory.getLockedStock());
        verify(reservationRepository).save(any(InventoryReservationEntity.class));
    }

    // ============================================
    // 测试2：订单取消释放库存
    // ============================================

    @Test
    @DisplayName("订单取消释放库存：locked=10 → available=100, locked=0, Reservation=RELEASED")
    void shouldReleaseInventoryOnOrderCancelled() {
        Inventory inventory = createInventory(90);
        inventory.setLockedStock(10);
        inventory.setStatus(InventoryStatus.LOCKED);

        InventoryReservationEntity reservation = InventoryReservationEntity.builder()
                .orderNo("ORDER001")
                .skuId(1001L)
                .quantity(10)
                .status(InvReservationStatus.LOCKED)
                .build();
        reservation.setId(1L);

        when(inventoryRepository.findBySkuId(1001L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByOrderNoAndSkuId("ORDER001", 1001L))
                .thenReturn(Optional.of(reservation));

        OrderCancelledEvent event = new OrderCancelledEvent("ORDER001", 1L, null, "测试取消",
                List.of(new OrderItemDto(1001L, 10)));

        orderCancelEventListener.onOrderCancelled(event);

        assertEquals(100, inventory.getAvailableStock());
        assertEquals(0, inventory.getLockedStock());
        assertEquals(InvReservationStatus.RELEASED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    // ============================================
    // 测试3：重复 OrderCreatedEvent（幂等）
    // ============================================

    @Test
    @DisplayName("重复 OrderCreatedEvent：第二次不重复锁库存，availableStock 不变")
    void shouldIgnoreDuplicateOrderCreatedEvent() {
        Inventory inventory = createInventory(90);
        inventory.setLockedStock(10);
        inventory.setStatus(InventoryStatus.LOCKED);

        when(reservationRepository.existsByOrderNoAndSkuId("ORDER001", 1001L)).thenReturn(true);

        OrderCreatedEvent event = new OrderCreatedEvent("ORDER001", 1L,
                List.of(new OrderItemDto(1001L, 10)));

        orderEventListener.onOrderCreated(event);

        // 未再次锁库存，库存不变
        assertEquals(90, inventory.getAvailableStock());
        assertEquals(10, inventory.getLockedStock());
        verify(reservationRepository, never()).save(any());
    }

    // ============================================
    // 测试4：重复取消事件（幂等）
    // ============================================

    @Test
    @DisplayName("重复取消事件：第二次已 RELEASED，不再释放，库存不变")
    void shouldIgnoreDuplicateCancelEvent() {
        Inventory inventory = createInventory(100);

        InventoryReservationEntity reservation = InventoryReservationEntity.builder()
                .orderNo("ORDER001")
                .skuId(1001L)
                .quantity(10)
                .status(InvReservationStatus.RELEASED)
                .build();
        reservation.setId(1L);

        when(reservationRepository.findByOrderNoAndSkuId("ORDER001", 1001L))
                .thenReturn(Optional.of(reservation));

        OrderCancelledEvent event = new OrderCancelledEvent("ORDER001", 1L, null, "重复取消",
                List.of(new OrderItemDto(1001L, 10)));

        orderCancelEventListener.onOrderCancelled(event);

        // 库存不变（不再释放）
        assertEquals(100, inventory.getAvailableStock());
        assertEquals(0, inventory.getLockedStock());
        assertEquals(InvReservationStatus.RELEASED, reservation.getStatus());
        verify(reservationRepository, never()).save(any());
    }
}