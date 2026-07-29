package com.commerce.platform.inventory.integration;

import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import com.commerce.platform.inventory.event.listener.OrderCancelEventListener;
import com.commerce.platform.inventory.event.listener.OrderEventListener;
import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;
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
 * Sprint 20 Step 4C: migrated to StockReservation (new model).
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class OrderInventoryIntegrationTest {

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    private InventoryApplicationService inventoryApplicationService;
    private OrderEventListener orderEventListener;
    private OrderCancelEventListener orderCancelEventListener;

    @BeforeEach
    void setUp() {
        inventoryApplicationService = new InventoryApplicationService(inventoryStockRepository, mock(org.springframework.context.ApplicationEventPublisher.class));
        orderEventListener = new OrderEventListener(inventoryApplicationService, stockReservationRepository);
        orderCancelEventListener = new OrderCancelEventListener(inventoryApplicationService, stockReservationRepository);
    }

    private InventoryStock createStock(int availableQuantity) {
        InventoryStock stock = InventoryStock.restore(1L, 1L, 1001L, availableQuantity, 0, 0, InventoryStatus.AVAILABLE);
        return stock;
    }

    // ============================================
    // 测试1：订单创建锁库存
    // ============================================

    @Test
    @DisplayName("订单创建锁库存：availableStock=100，锁10 → available=90, locked=10")
    void shouldLockInventoryOnOrderCreated() {
        InventoryStock stock = createStock(100);

        when(inventoryStockRepository.findBySkuId(1001L)).thenReturn(Optional.of(stock));
        when(stockReservationRepository.existsByOrderIdAndProductId(1L, 1L)).thenReturn(false);

        OrderCreatedEvent event = new OrderCreatedEvent(1L, "ORDER001", 1L,
                List.of(new OrderItemDto(1001L, 1L, 10)));

        orderEventListener.onOrderCreated(event);

        assertEquals(90, stock.getAvailableQuantity());
        assertEquals(10, stock.getReservedQuantity());
        verify(stockReservationRepository).save(any(StockReservation.class));
    }

    // ============================================
    // 测试2：订单取消释放库存
    // ============================================

    @Test
    @DisplayName("订单取消释放库存：locked=10 → available=100, locked=0, Reservation=RELEASED")
    void shouldReleaseInventoryOnOrderCancelled() {
        // Restore with pre-reserved state: available=90, reserved=10
        InventoryStock stock = InventoryStock.restore(1L, 1L, 1001L, 90, 10, 0, InventoryStatus.LOCKED);
        stock.setId(1L);

        StockReservation reservation = StockReservation.create(1L, 1L, 10);
        reservation.setId(1L);

        when(inventoryStockRepository.findBySkuId(1001L)).thenReturn(Optional.of(stock));
        when(stockReservationRepository.findByOrderIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(reservation));

        OrderCancelledEvent event = new OrderCancelledEvent(1L, "ORDER001", 1L, null, "测试取消",
                List.of(new OrderItemDto(1001L, 1L, 10)));

        orderCancelEventListener.onOrderCancelled(event);

        assertEquals(100, stock.getAvailableQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        verify(stockReservationRepository).save(reservation);
    }

    // ============================================
    // 测试3：重复 OrderCreatedEvent（幂等）
    // ============================================

    @Test
    @DisplayName("重复 OrderCreatedEvent：第二次不重复锁库存，availableStock 不变")
    void shouldIgnoreDuplicateOrderCreatedEvent() {
        // Restore with pre-reserved state: available=90, reserved=10
        InventoryStock stock = InventoryStock.restore(1L, 1L, 1001L, 90, 10, 0, InventoryStatus.LOCKED);
        stock.setId(1L);

        when(stockReservationRepository.existsByOrderIdAndProductId(1L, 1L)).thenReturn(true);

        OrderCreatedEvent event = new OrderCreatedEvent(1L, "ORDER001", 1L,
                List.of(new OrderItemDto(1001L, 1L, 10)));

        orderEventListener.onOrderCreated(event);

        // 未再次锁库存，库存不变
        assertEquals(90, stock.getAvailableQuantity());
        assertEquals(10, stock.getReservedQuantity());
        verify(stockReservationRepository, never()).save(any());
    }

    // ============================================
    // 测试4：重复取消事件（幂等）
    // ============================================

    @Test
    @DisplayName("重复取消事件：第二次已 RELEASED，不再释放，库存不变")
    void shouldIgnoreDuplicateCancelEvent() {
        InventoryStock stock = createStock(100);

        StockReservation reservation = StockReservation.create(1L, 1L, 10);
        reservation.setId(1L);
        reservation.release();

        when(stockReservationRepository.findByOrderIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(reservation));

        OrderCancelledEvent event = new OrderCancelledEvent(1L, "ORDER001", 1L, null, "重复取消",
                List.of(new OrderItemDto(1001L, 1L, 10)));

        orderCancelEventListener.onOrderCancelled(event);

        // 库存不变（不再释放）
        assertEquals(100, stock.getAvailableQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        verify(stockReservationRepository, never()).save(any());
    }
}