package com.commerce.platform.warehouse.domain.entity;

import com.commerce.platform.warehouse.domain.aggregate.Warehouse;
import com.commerce.platform.warehouse.domain.exception.InvalidWarehouseStatusException;
import com.commerce.platform.warehouse.domain.valueobject.WarehouseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Warehouse 状态流转测试")
class WarehouseStateTest {

    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.create("WH-001", "Main Warehouse", "Shanghai");
    }

    @Test
    @DisplayName("创建仓库应初始化为 INACTIVE")
    void shouldBeInactiveWhenCreated() {
        assertEquals(WarehouseStatus.INACTIVE, warehouse.getStatus());
        assertEquals("WH-001", warehouse.getCode());
        assertEquals("Main Warehouse", warehouse.getName());
    }

    @Test
    @DisplayName("INACTIVE → ACTIVE 应合法")
    void shouldActivateFromInactive() {
        warehouse.activate();
        assertEquals(WarehouseStatus.ACTIVE, warehouse.getStatus());
        assertTrue(warehouse.isAvailable());
    }

    @Test
    @DisplayName("ACTIVE → INACTIVE 应合法")
    void shouldDisableFromActive() {
        warehouse.activate();
        warehouse.disable();
        assertEquals(WarehouseStatus.INACTIVE, warehouse.getStatus());
        assertFalse(warehouse.isAvailable());
    }

    @Test
    @DisplayName("ACTIVE → MAINTENANCE 应合法")
    void shouldSetMaintenanceFromActive() {
        warehouse.activate();
        warehouse.maintenance();
        assertEquals(WarehouseStatus.MAINTENANCE, warehouse.getStatus());
        assertFalse(warehouse.isAvailable());
    }

    @Test
    @DisplayName("MAINTENANCE → ACTIVE 应合法")
    void shouldActivateFromMaintenance() {
        warehouse.activate();
        warehouse.maintenance();
        warehouse.activate();
        assertEquals(WarehouseStatus.ACTIVE, warehouse.getStatus());
    }

    @Test
    @DisplayName("INACTIVE → MAINTENANCE 应抛异常")
    void shouldNotAllowInactiveToMaintenance() {
        assertThrows(InvalidWarehouseStatusException.class, () -> warehouse.maintenance());
    }

    @Test
    @DisplayName("INACTIVE → INACTIVE 应抛异常")
    void shouldNotAllowInactiveToInactive() {
        assertThrows(InvalidWarehouseStatusException.class, () -> warehouse.disable());
    }

    @Test
    @DisplayName("MAINTENANCE → INACTIVE 应抛异常")
    void shouldNotAllowMaintenanceToInactive() {
        warehouse.activate();
        warehouse.maintenance();
        assertThrows(InvalidWarehouseStatusException.class, () -> warehouse.disable());
    }
}