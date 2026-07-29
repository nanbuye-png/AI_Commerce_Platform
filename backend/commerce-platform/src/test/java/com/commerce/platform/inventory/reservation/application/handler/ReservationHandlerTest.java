package com.commerce.platform.inventory.reservation.application.handler;

import com.commerce.platform.inventory.reservation.application.command.ConfirmStockCommand;
import com.commerce.platform.inventory.reservation.application.command.ReleaseStockCommand;
import com.commerce.platform.inventory.reservation.application.command.ReserveStockCommand;
import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.event.StockConfirmedEvent;
import com.commerce.platform.inventory.reservation.domain.event.StockReleasedEvent;
import com.commerce.platform.inventory.reservation.domain.event.StockReservedEvent;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.exception.InsufficientStockException;
import com.commerce.platform.inventory.stock.domain.repository.InventoryRepository;
import com.commerce.platform.inventory.stock.domain.service.InventoryReleaseService;
import com.commerce.platform.inventory.stock.domain.service.InventoryReservationService;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 库存预占命令处理器测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Reservation Handler 测试")
class ReservationHandlerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InventoryReservationService inventoryReservationService;
    private InventoryReleaseService inventoryReleaseService;
    private ReserveStockHandler reserveStockHandler;
    private ConfirmStockHandler confirmStockHandler;
    private ReleaseStockHandler releaseStockHandler;

    @BeforeEach
    void setUp() {
        inventoryReservationService = new InventoryReservationService(inventoryRepository);
        inventoryReleaseService = new InventoryReleaseService(inventoryRepository);
        reserveStockHandler = new ReserveStockHandler(
                inventoryRepository, stockReservationRepository,
                inventoryReservationService, eventPublisher);
        confirmStockHandler = new ConfirmStockHandler(stockReservationRepository, eventPublisher);
        releaseStockHandler = new ReleaseStockHandler(
                stockReservationRepository, inventoryRepository,
                inventoryReleaseService, eventPublisher);
    }

    @Test
    @DisplayName("Reserve 应检查库存、保存并发布事件")
    void shouldSaveAndPublishEventOnReserve() {
        ReserveStockCommand command = new ReserveStockCommand(1L, 1L, 5);

        InventoryStock inventory = InventoryStock.create(1L, 100L, 50);
        inventory.setId(1L);

        StockReservation savedReservation = StockReservation.create(1L, 1L, 5);
        savedReservation.setId(1L);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(stockReservationRepository.save(any(StockReservation.class)))
                .thenReturn(savedReservation);

        StockReservation result = reserveStockHandler.handle(command);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals(1L, result.getProductId());
        assertEquals(5, result.getQuantity());
        assertEquals(ReservationStatus.RESERVED, result.getStatus());

        assertEquals(45, inventory.getAvailableQuantity());
        assertEquals(5, inventory.getReservedQuantity());

        verify(inventoryRepository, times(1)).findByProductId(1L);
        verify(inventoryRepository, times(1)).save(inventory);
        verify(stockReservationRepository, times(1)).save(any(StockReservation.class));

        ArgumentCaptor<StockReservedEvent> eventCaptor = ArgumentCaptor.forClass(StockReservedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        StockReservedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getOrderId());
        assertEquals(5, event.getQuantity());
    }

    @Test
    @DisplayName("Reserve 库存不足时应抛异常")
    void shouldThrowExceptionWhenInsufficientStock() {
        ReserveStockCommand command = new ReserveStockCommand(1L, 1L, 100);

        InventoryStock inventory = InventoryStock.create(1L, 100L, 50);
        inventory.setId(1L);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> reserveStockHandler.handle(command));

        verify(inventoryRepository, times(1)).findByProductId(1L);
        verify(inventoryRepository, never()).save(any());
        verify(stockReservationRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Reserve 库存不存在时应抛异常")
    void shouldThrowExceptionWhenInventoryNotFound() {
        ReserveStockCommand command = new ReserveStockCommand(1L, 999L, 5);
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reserveStockHandler.handle(command));
        verify(inventoryRepository, times(1)).findByProductId(999L);
        verify(stockReservationRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Confirm 应查找、确认、保存并发布事件")
    void shouldFindConfirmSaveAndPublishOnConfirm() {
        ConfirmStockCommand command = new ConfirmStockCommand(1L);

        StockReservation existingReservation = StockReservation.create(1L, 100L, 5);
        existingReservation.setId(1L);

        StockReservation confirmedReservation = StockReservation.create(1L, 100L, 5);
        confirmedReservation.setId(1L);
        confirmedReservation.confirm();

        when(stockReservationRepository.findById(1L)).thenReturn(Optional.of(existingReservation));
        when(stockReservationRepository.save(any(StockReservation.class)))
                .thenReturn(confirmedReservation);

        StockReservation result = confirmStockHandler.handle(command);

        assertNotNull(result);
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());

        verify(stockReservationRepository, times(1)).findById(1L);
        verify(stockReservationRepository, times(1)).save(any(StockReservation.class));

        ArgumentCaptor<StockConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(StockConfirmedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        StockConfirmedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getReservationId());
    }

    @Test
    @DisplayName("Release 应释放库存、保存并发布事件")
    void shouldReleaseSaveAndPublishOnRelease() {
        ReleaseStockCommand command = new ReleaseStockCommand(1L);

        InventoryStock inventory = InventoryStock.create(1L, 100L, 50);
        inventory.reserve(5);
        inventory.setId(1L);

        StockReservation existingReservation = StockReservation.create(1L, 1L, 5);
        existingReservation.setId(1L);

        StockReservation savedReservation = StockReservation.create(1L, 1L, 5);
        savedReservation.setId(1L);
        savedReservation.release();

        when(stockReservationRepository.findById(1L)).thenReturn(Optional.of(existingReservation));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(stockReservationRepository.save(any(StockReservation.class))).thenReturn(savedReservation);

        StockReservation result = releaseStockHandler.handle(command);

        assertNotNull(result);
        assertEquals(ReservationStatus.RELEASED, result.getStatus());

        verify(stockReservationRepository, times(1)).findById(1L);
        verify(inventoryRepository, times(1)).findByProductId(1L);
        verify(inventoryRepository, times(1)).save(any(InventoryStock.class));
        verify(stockReservationRepository, times(1)).save(any(StockReservation.class));

        ArgumentCaptor<StockReleasedEvent> eventCaptor = ArgumentCaptor.forClass(StockReleasedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        StockReleasedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getReservationId());
    }

    @Test
    @DisplayName("Release 重复调用应幂等忽略")
    void shouldBeIdempotentOnRelease() {
        ReleaseStockCommand command = new ReleaseStockCommand(1L);

        StockReservation alreadyReleased = StockReservation.create(1L, 1L, 5);
        alreadyReleased.setId(1L);
        alreadyReleased.release();

        when(stockReservationRepository.findById(1L)).thenReturn(Optional.of(alreadyReleased));

        StockReservation result = releaseStockHandler.handle(command);

        assertNotNull(result);
        assertEquals(ReservationStatus.RELEASED, result.getStatus());

        // 幂等：不应保存或发布事件
        verify(inventoryRepository, never()).findByProductId(any());
        verify(inventoryRepository, never()).save(any());
        verify(stockReservationRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Confirm 预占不存在时应抛异常")
    void shouldThrowExceptionWhenReservationNotFoundOnConfirm() {
        ConfirmStockCommand command = new ConfirmStockCommand(999L);
        when(stockReservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> confirmStockHandler.handle(command));
    }

    @Test
    @DisplayName("Release 预占不存在时应抛异常")
    void shouldThrowExceptionWhenReservationNotFoundOnRelease() {
        ReleaseStockCommand command = new ReleaseStockCommand(999L);
        when(stockReservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> releaseStockHandler.handle(command));
    }
}