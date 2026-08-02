package com.commerce.platform.inventory.stock.domain.entity;

import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InventoryStock 聚合测试
 */
@DisplayName("InventoryStock 库存预占模型测试")
class InventoryStockTest {

    private InventoryStock inventory;

    @BeforeEach
    void setUp() {
        inventory = InventoryStock.create(1L, 100L, 50);
    }

    @Test
    @DisplayName("创建库存应正确初始化")
    void shouldCreateInventoryCorrectly() {
        assertEquals(1L, inventory.getProductId());
        assertEquals(100L, inventory.getSkuId());
        assertEquals(50, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(0, inventory.getSoldQuantity());
        assertEquals(50, inventory.getTotalQuantity());
    }

    @Test
    @DisplayName("reserve 成功应减少可用库存并增加预占库存")
    void shouldReserveSuccessfully() {
        inventory.reserve(10);
        assertEquals(40, inventory.getAvailableQuantity());
        assertEquals(10, inventory.getReservedQuantity());
        assertEquals(0, inventory.getSoldQuantity());
    }

    @Test
    @DisplayName("reserve 超库存应抛异常")
    void shouldThrowExceptionWhenReserveExceedsAvailable() {
        assertThrows(InsufficientStockException.class, () -> inventory.reserve(100));
        assertEquals(50, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("reserve 精确等于可用库存应成功")
    void shouldReserveAllAvailableStock() {
        inventory.reserve(50);
        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(50, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("release 应增加可用库存并减少预占库存")
    void shouldReleaseSuccessfully() {
        inventory.reserve(20);
        inventory.release(10);
        assertEquals(40, inventory.getAvailableQuantity());
        assertEquals(10, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("release 全部应恢复初始状态")
    void shouldReleaseAll() {
        inventory.reserve(20);
        inventory.release(20);
        assertEquals(50, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("release 超出预占库存应抛异常")
    void shouldThrowExceptionWhenReleaseExceedsReserved() {
        inventory.reserve(10);
        assertThrows(IllegalStateException.class, () -> inventory.release(20));
    }

    @Test
    @DisplayName("confirm 应减少预占库存并增加已售库存")
    void shouldConfirmSuccessfully() {
        inventory.reserve(20);
        inventory.confirm(15);
        assertEquals(30, inventory.getAvailableQuantity());
        assertEquals(5, inventory.getReservedQuantity());
        assertEquals(15, inventory.getSoldQuantity());
    }

    @Test
    @DisplayName("confirm 全部应清空预占")
    void shouldConfirmAll() {
        inventory.reserve(20);
        inventory.confirm(20);
        assertEquals(30, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(20, inventory.getSoldQuantity());
    }

    @Test
    @DisplayName("confirm 超出预占库存应抛异常")
    void shouldThrowExceptionWhenConfirmExceedsReserved() {
        inventory.reserve(10);
        assertThrows(IllegalStateException.class, () -> inventory.confirm(20));
    }

    @Test
    @DisplayName("restore 应正确恢复所有字段")
    void shouldRestoreAllFields() {
        InventoryStock restored = InventoryStock.restore(1L, 1L, 100L, 30, 10, 10);

        assertEquals(1L, restored.getId());
        assertEquals(1L, restored.getProductId());
        assertEquals(100L, restored.getSkuId());
        assertEquals(30, restored.getAvailableQuantity());
        assertEquals(10, restored.getReservedQuantity());
        assertEquals(10, restored.getSoldQuantity());
        assertEquals(40, restored.getTotalQuantity());
    }

    @Test
    @DisplayName("多次 reserve 和 release 应正确累计")
    void shouldHandleMultipleReserveAndRelease() {
        inventory.reserve(10);
        inventory.reserve(5);
        assertEquals(35, inventory.getAvailableQuantity());
        assertEquals(15, inventory.getReservedQuantity());

        inventory.release(5);
        assertEquals(40, inventory.getAvailableQuantity());
        assertEquals(10, inventory.getReservedQuantity());

        inventory.confirm(5);
        assertEquals(40, inventory.getAvailableQuantity());
        assertEquals(5, inventory.getReservedQuantity());
        assertEquals(5, inventory.getSoldQuantity());
    }
}