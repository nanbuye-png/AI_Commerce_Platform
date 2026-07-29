package com.commerce.platform.inventory.domain.entity;

import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InventoryStock Aggregate 领域行为覆盖测试
 * <p>
 * Sprint 21 Step 2C: Migrated from Inventory Entity to InventoryStock Aggregate.
 * 测试预占(reserve)/释放(release)/确认(confirm)/调整(adjust)/入库(inbound) 的正向和异常路径。
 * </p>
 */
class InventoryStateTest {

    private InventoryStock createStock(int availableQuantity, int reservedQuantity, int soldQuantity) {
        return InventoryStock.restore(1L, 1L, 1001L, availableQuantity, reservedQuantity, soldQuantity, InventoryStatus.AVAILABLE);
    }

    // ======== 正向路径 — reserve ========

    @Test
    @DisplayName("正向路径：reserve(10) → available-=10, reserved+=10")
    void shouldReserveStockSuccessfully() {
        InventoryStock stock = createStock(100, 0, 0);

        stock.reserve(10);
        assertEquals(90, stock.getAvailableQuantity());
        assertEquals(10, stock.getReservedQuantity());
        assertEquals(0, stock.getSoldQuantity());
    }

    @Test
    @DisplayName("正向路径：多次 reserve → available递减, reserved递增")
    void shouldAllowMultipleReserve() {
        InventoryStock stock = createStock(50, 5, 0);

        stock.reserve(10);
        assertEquals(40, stock.getAvailableQuantity());
        assertEquals(15, stock.getReservedQuantity());
    }

    // ======== 正向路径 — release ========

    @Test
    @DisplayName("正向路径：release(10) → reserved-=10, available+=10")
    void shouldReleaseStockSuccessfully() {
        InventoryStock stock = createStock(90, 10, 0);

        stock.release(10);
        assertEquals(100, stock.getAvailableQuantity());
        assertEquals(0, stock.getReservedQuantity());
    }

    // ======== 正向路径 — confirm ========

    @Test
    @DisplayName("正向路径：confirm(10) → reserved-=10, sold+=10")
    void shouldConfirmStockSuccessfully() {
        InventoryStock stock = createStock(90, 10, 0);

        stock.confirm(10);
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(10, stock.getSoldQuantity());
    }

    // ======== 正向路径 — adjust / inbound ========

    @Test
    @DisplayName("正向路径：adjust(10) → available+=10")
    void shouldAdjustIncreaseSuccessfully() {
        InventoryStock stock = createStock(100, 0, 0);

        stock.adjust(10);
        assertEquals(110, stock.getAvailableQuantity());
    }

    @Test
    @DisplayName("正向路径：adjust(-10) → available-=10")
    void shouldAdjustDecreaseSuccessfully() {
        InventoryStock stock = createStock(100, 0, 0);

        stock.adjust(-10);
        assertEquals(90, stock.getAvailableQuantity());
    }

    @Test
    @DisplayName("正向路径：inbound(10) → available+=10")
    void shouldInboundSuccessfully() {
        InventoryStock stock = createStock(100, 0, 0);

        stock.inbound(10);
        assertEquals(110, stock.getAvailableQuantity());
    }

    // ======== 异常路径 ========

    @Test
    @DisplayName("异常路径：库存不足时 reserve() 抛出 InsufficientStockException")
    void shouldThrowWhenInsufficientStock() {
        InventoryStock stock = createStock(5, 0, 0);

        assertThrows(InsufficientStockException.class, () -> stock.reserve(10));
    }

    @Test
    @DisplayName("异常路径：reserve(0) 抛出 IllegalArgumentException")
    void shouldThrowWhenReserveZero() {
        InventoryStock stock = createStock(10, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> stock.reserve(0));
    }

    @Test
    @DisplayName("异常路径：release(null) 抛出 IllegalArgumentException")
    void shouldThrowWhenReleaseNull() {
        InventoryStock stock = createStock(10, 10, 0);

        assertThrows(IllegalArgumentException.class, () -> stock.release(null));
    }

    @Test
    @DisplayName("异常路径：预占不足时 release() 抛出 IllegalStateException")
    void shouldThrowWhenReleaseExceedsReserved() {
        InventoryStock stock = createStock(90, 5, 0);

        assertThrows(IllegalStateException.class, () -> stock.release(10));
    }

    @Test
    @DisplayName("异常路径：confirm(0) 抛出 IllegalArgumentException")
    void shouldThrowWhenConfirmZero() {
        InventoryStock stock = createStock(10, 10, 0);

        assertThrows(IllegalArgumentException.class, () -> stock.confirm(0));
    }

    @Test
    @DisplayName("异常路径：adjust(0) 抛出 IllegalArgumentException")
    void shouldThrowWhenAdjustZero() {
        InventoryStock stock = createStock(10, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> stock.adjust(0));
    }

    @Test
    @DisplayName("异常路径：减少超过可售库存时 adjust() 抛出 IllegalStateException")
    void shouldThrowWhenDecreaseExceedsAvailable() {
        InventoryStock stock = createStock(5, 0, 0);

        assertThrows(IllegalStateException.class, () -> stock.adjust(-10));
    }

    @Test
    @DisplayName("异常路径：inbound(0) 抛出 IllegalArgumentException")
    void shouldThrowWhenInboundZero() {
        InventoryStock stock = createStock(10, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> stock.inbound(0));
    }

    // ======== Getters ========

    @Test
    @DisplayName("getTotalQuantity 返回 available + reserved")
    void shouldReturnCorrectTotalQuantity() {
        InventoryStock stock = createStock(80, 20, 0);

        assertEquals(100, stock.getTotalQuantity());
    }

    @Test
    @DisplayName("getStatus 返回 AVAILABLE")
    void shouldReturnAvailableStatus() {
        InventoryStock stock = createStock(100, 0, 0);

        assertEquals(InventoryStatus.AVAILABLE, stock.getStatus());
    }
}