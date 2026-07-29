package com.commerce.platform.warehouse.domain.service;

import com.commerce.platform.warehouse.domain.aggregate.PickingTask;
import com.commerce.platform.warehouse.domain.aggregate.Warehouse;
import com.commerce.platform.warehouse.domain.repository.PickingTaskRepository;
import com.commerce.platform.warehouse.domain.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseDomainService 测试")
class WarehouseDomainServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private PickingTaskRepository pickingTaskRepository;

    private WarehouseDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new WarehouseDomainService(warehouseRepository, pickingTaskRepository);
    }

    @Test
    @DisplayName("有可用仓库时应成功创建拣货任务")
    void shouldCreatePickingTaskWhenWarehouseAvailable() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main", "Addr");
        warehouse.activate();
        warehouse.setId(1L);
        when(warehouseRepository.findActiveWarehouse()).thenReturn(Optional.of(warehouse));

        PickingTask result = domainService.createPickingTask(100L);

        assertNotNull(result);
        assertEquals(100L, result.getFulfillmentId());
        assertEquals(1L, result.getWarehouseId());
        verify(warehouseRepository).findActiveWarehouse();
    }

    @Test
    @DisplayName("无可用仓库时应抛出异常")
    void shouldThrowExceptionWhenNoActiveWarehouse() {
        when(warehouseRepository.findActiveWarehouse()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> domainService.createPickingTask(100L));
    }
}