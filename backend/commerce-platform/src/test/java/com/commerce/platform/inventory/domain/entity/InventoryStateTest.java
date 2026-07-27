package com.commerce.platform.inventory.domain.entity;

import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import com.commerce.platform.inventory.exception.InsufficientInventoryException;
import com.commerce.platform.inventory.exception.InvalidInventoryStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Inventory Entity 状态转换覆盖测试
 * <p>
 * 覆盖所有合法路径和非法路径。
 * 符合任务书中定义的 Inventory 状态机规则。
 * </p>
 */
class InventoryStateTest {

    private Inventory createInventory(InventoryStatus status, int availableStock) {
        return Inventory.builder()
                .productId(1L)
                .skuId(1001L)
                .availableStock(availableStock)
                .lockedStock(0)
                .soldStock(0)
                .status(status)
                .build();
    }

    // ======== 正向路径 ========

    @Test
    @DisplayName("正向路径：AVAILABLE → LOCKED → DEDUCTED")
    void shouldFollowAvailableToLockedToDeducted() {
        Inventory inventory = createInventory(InventoryStatus.AVAILABLE, 10);

        inventory.lockStock();
        assertEquals(InventoryStatus.LOCKED, inventory.getStatus());
        assertEquals(9, inventory.getAvailableStock());
        assertEquals(1, inventory.getLockedStock());

        inventory.deductStock();
        assertEquals(InventoryStatus.DEDUCTED, inventory.getStatus());
        assertEquals(0, inventory.getLockedStock());
        assertEquals(1, inventory.getSoldStock());
    }

    @Test
    @DisplayName("正向路径：LOCKED → RELEASED")
    void shouldFollowLockedToReleased() {
        Inventory inventory = createInventory(InventoryStatus.LOCKED, 5);
        inventory.setLockedStock(1);

        inventory.releaseStock();
        assertEquals(InventoryStatus.RELEASED, inventory.getStatus());
        assertEquals(6, inventory.getAvailableStock());
        assertEquals(0, inventory.getLockedStock());
    }

    @Test
    @DisplayName("正向路径：DEDUCTED → AVAILABLE（恢复库存/退款场景）")
    void shouldFollowDeductedToAvailable() {
        Inventory inventory = createInventory(InventoryStatus.DEDUCTED, 5);
        inventory.setSoldStock(2);

        inventory.restoreStock();
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
        assertEquals(6, inventory.getAvailableStock());
        assertEquals(1, inventory.getSoldStock());
    }

    @Test
    @DisplayName("正向路径：LOCKED 状态下可继续锁定（批量锁定场景）")
    void shouldAllowLockedToLockBatch() {
        Inventory inventory = createInventory(InventoryStatus.LOCKED, 5);
        inventory.setLockedStock(5);

        inventory.lockStock();
        assertEquals(InventoryStatus.LOCKED, inventory.getStatus());
        assertEquals(4, inventory.getAvailableStock());
        assertEquals(6, inventory.getLockedStock());
    }

    @Test
    @DisplayName("正向路径：RELEASED 状态下可继续释放（批量释放场景）")
    void shouldAllowReleasedToReleaseBatch() {
        Inventory inventory = createInventory(InventoryStatus.RELEASED, 10);
        inventory.setLockedStock(5);

        inventory.releaseStock();
        assertEquals(InventoryStatus.RELEASED, inventory.getStatus());
        assertEquals(11, inventory.getAvailableStock());
        assertEquals(4, inventory.getLockedStock());
    }

    // ======== 异常路径 ========

    @Test
    @DisplayName("异常路径：库存不足时 lockStock() 抛出 InsufficientInventoryException")
    void shouldThrowWhenInsufficientStock() {
        Inventory inventory = createInventory(InventoryStatus.AVAILABLE, 0);

        assertThrows(InsufficientInventoryException.class, inventory::lockStock);
    }

    @Test
    @DisplayName("异常路径：AVAILABLE.deductStock() 抛出 InvalidInventoryStatusException")
    void shouldNotAllowAvailableToDeduct() {
        Inventory inventory = createInventory(InventoryStatus.AVAILABLE, 10);

        assertThrows(InvalidInventoryStatusException.class, inventory::deductStock);
    }

    @Test
    @DisplayName("异常路径：DEDUCTED.lockStock() 抛出 InvalidInventoryStatusException")
    void shouldNotAllowDeductedToLock() {
        Inventory inventory = createInventory(InventoryStatus.DEDUCTED, 5);

        assertThrows(InvalidInventoryStatusException.class, inventory::lockStock);
    }

    @Test
    @DisplayName("异常路径：RELEASED.deductStock() 抛出 InvalidInventoryStatusException")
    void shouldNotAllowReleasedToDeduct() {
        Inventory inventory = createInventory(InventoryStatus.RELEASED, 5);

        assertThrows(InvalidInventoryStatusException.class, inventory::deductStock);
    }

    @Test
    @DisplayName("异常路径：AVAILABLE.releaseStock() 抛出 InvalidInventoryStatusException")
    void shouldNotAllowAvailableToRelease() {
        Inventory inventory = createInventory(InventoryStatus.AVAILABLE, 10);

        assertThrows(InvalidInventoryStatusException.class, inventory::releaseStock);
    }

    @Test
    @DisplayName("异常路径：AVAILABLE.restoreStock() 抛出 InvalidInventoryStatusException")
    void shouldNotAllowAvailableToRestore() {
        Inventory inventory = createInventory(InventoryStatus.AVAILABLE, 10);

        assertThrows(InvalidInventoryStatusException.class, inventory::restoreStock);
    }

    @Test
    @DisplayName("异常路径：RELEASED.lockStock() 抛出 InvalidInventoryStatusException")
    void shouldNotAllowReleasedToLock() {
        Inventory inventory = createInventory(InventoryStatus.RELEASED, 5);

        assertThrows(InvalidInventoryStatusException.class, inventory::lockStock);
    }
}