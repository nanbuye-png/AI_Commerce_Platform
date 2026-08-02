package com.commerce.platform.inventory.service;

import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.dto.request.CreateInventoryRequest;
import com.commerce.platform.inventory.dto.response.InventoryVO;
import com.commerce.platform.inventory.event.InventoryLockedEvent;
import com.commerce.platform.inventory.event.InventoryReleasedEvent;
import com.commerce.platform.inventory.exception.InsufficientInventoryException;
import com.commerce.platform.inventory.exception.InventoryAlreadyExistsException;
import com.commerce.platform.inventory.exception.InventoryNotFoundException;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.exception.InsufficientStockException;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Inventory Application Service 测试
 * <p>
 * 覆盖：创建库存、锁定库存、库存不足、释放库存、Event 发布。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class InventoryApplicationServiceTest {

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<InventoryLockedEvent> lockedEventCaptor;

    @Captor
    private ArgumentCaptor<InventoryReleasedEvent> releasedEventCaptor;

    private InventoryApplicationService inventoryApplicationService;

    @BeforeEach
    void setUp() {
        inventoryApplicationService = new InventoryApplicationService(inventoryStockRepository, eventPublisher);
    }

    private InventoryStock createStock(Long id, Long productId, Long skuId,
                                       int availableQuantity, int reservedQuantity, int soldQuantity,
                                       InventoryStatus status) {
        InventoryStock stock = InventoryStock.restore(id, productId, skuId, availableQuantity, reservedQuantity, soldQuantity, status);
        return stock;
    }

    // ============================================
    // 1. 创建库存
    // ============================================

    @Test
    @DisplayName("创建库存：输入 skuId=1001, stock=100，验证 availableStock=100")
    void shouldCreateInventorySuccessfully() {
        CreateInventoryRequest request = new CreateInventoryRequest();
        request.setProductId(1L);
        request.setSkuId(1001L);
        request.setInitialStock(100);

        when(inventoryStockRepository.existsBySkuId(1001L)).thenReturn(false);

        InventoryStock savedStock = createStock(1L, 1L, 1001L, 100, 0, 0, InventoryStatus.AVAILABLE);
        when(inventoryStockRepository.save(any(InventoryStock.class))).thenReturn(savedStock);

        InventoryVO vo = inventoryApplicationService.createInventory(request);

        assertNotNull(vo);
        assertEquals(1001L, vo.getSkuId());
        assertEquals(100, vo.getAvailableStock());
        assertEquals(0, vo.getLockedStock());
        assertEquals(0, vo.getSoldStock());
        assertEquals(InventoryStatus.AVAILABLE.name(), vo.getStatus());
    }

    // ============================================
    // 2. 正常锁库存
    // ============================================

    @Test
    @DisplayName("正常锁库存：available=100，锁 10，验证 available=90, locked=10")
    void shouldLockInventorySuccessfully() {
        InventoryStock stock = createStock(1L, 1L, 1001L, 100, 0, 0, InventoryStatus.AVAILABLE);

        when(inventoryStockRepository.findBySkuId(1001L)).thenReturn(Optional.of(stock));

        InventoryVO vo = inventoryApplicationService.lockInventory(1001L, 10, "ORDER_001");

        assertNotNull(vo);
        assertEquals(90, vo.getAvailableStock());
        assertEquals(10, vo.getLockedStock());
    }

    // ============================================
    // 3. 库存不足
    // ============================================

    @Test
    @DisplayName("库存不足：available=5，请求 10，抛 InsufficientInventoryException")
    void shouldThrowWhenInsufficientStock() {
        InventoryStock stock = createStock(1L, 1L, 1001L, 5, 0, 0, InventoryStatus.AVAILABLE);

        when(inventoryStockRepository.findBySkuId(1001L)).thenReturn(Optional.of(stock));

        assertThrows(InsufficientStockException.class,
                () -> inventoryApplicationService.lockInventory(1001L, 10, "ORDER_001"));
    }

    // ============================================
    // 4. 释放库存
    // ============================================

    @Test
    @DisplayName("释放库存：锁 10 后释放 10，验证 available 恢复, locked=0")
    void shouldReleaseInventorySuccessfully() {
        InventoryStock stock = createStock(1L, 1L, 1001L, 90, 10, 0, InventoryStatus.LOCKED);

        when(inventoryStockRepository.findBySkuId(1001L)).thenReturn(Optional.of(stock));

        InventoryVO vo = inventoryApplicationService.releaseInventory(1001L, 10, "ORDER_001");

        assertNotNull(vo);
        assertEquals(100, vo.getAvailableStock());
        assertEquals(0, vo.getLockedStock());
    }

    // ============================================
    // 5. Event 发布测试
    // ============================================

    @Test
    @DisplayName("lockInventory 发布 InventoryLockedEvent")
    void shouldPublishLockedEvent() {
        InventoryStock stock = createStock(1L, 1L, 1001L, 100, 0, 0, InventoryStatus.AVAILABLE);

        when(inventoryStockRepository.findBySkuId(1001L)).thenReturn(Optional.of(stock));

        inventoryApplicationService.lockInventory(1001L, 10, "ORDER_001");

        verify(eventPublisher).publishEvent(lockedEventCaptor.capture());
        InventoryLockedEvent event = lockedEventCaptor.getValue();

        assertEquals(1001L, event.getSkuId());
        assertEquals(10, event.getQuantity());
        assertEquals("ORDER_001", event.getOrderNo());
        assertNotNull(event.getLockTime());
    }

    @Test
    @DisplayName("releaseInventory 发布 InventoryReleasedEvent")
    void shouldPublishReleasedEvent() {
        InventoryStock stock = createStock(1L, 1L, 1001L, 90, 10, 0, InventoryStatus.LOCKED);

        when(inventoryStockRepository.findBySkuId(1001L)).thenReturn(Optional.of(stock));

        inventoryApplicationService.releaseInventory(1001L, 10, "ORDER_001");

        verify(eventPublisher).publishEvent(releasedEventCaptor.capture());
        InventoryReleasedEvent event = releasedEventCaptor.getValue();

        assertEquals(1001L, event.getSkuId());
        assertEquals(10, event.getQuantity());
        assertEquals("ORDER_001", event.getOrderNo());
        assertNotNull(event.getReleaseTime());
    }

    // ============================================
    // 6. 库存不存在异常
    // ============================================

    @Test
    @DisplayName("锁定不存在的库存，抛 InventoryNotFoundException")
    void shouldThrowWhenInventoryNotFound() {
        when(inventoryStockRepository.findBySkuId(9999L)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryApplicationService.lockInventory(9999L, 1, "ORDER_001"));
    }

    @Test
    @DisplayName("查询不存在的库存，抛 InventoryNotFoundException")
    void shouldThrowWhenGetInventoryNotFound() {
        when(inventoryStockRepository.findBySkuId(9999L)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryApplicationService.getInventory(9999L));
    }

    @Test
    @DisplayName("创建已存在的 SKU，抛 InventoryAlreadyExistsException")
    void shouldThrowWhenSkuAlreadyExists() {
        CreateInventoryRequest request = new CreateInventoryRequest();
        request.setProductId(1L);
        request.setSkuId(1001L);
        request.setInitialStock(100);

        when(inventoryStockRepository.existsBySkuId(1001L)).thenReturn(true);

        assertThrows(InventoryAlreadyExistsException.class,
                () -> inventoryApplicationService.createInventory(request));
    }
}