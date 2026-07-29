package com.commerce.platform.warehouse.application.handler;

import com.commerce.platform.warehouse.application.command.CreatePickingTaskCommand;
import com.commerce.platform.warehouse.domain.aggregate.PickingTask;
import com.commerce.platform.warehouse.domain.aggregate.Warehouse;
import com.commerce.platform.warehouse.domain.event.PickingTaskCreatedEvent;
import com.commerce.platform.warehouse.domain.repository.PickingTaskRepository;
import com.commerce.platform.warehouse.domain.repository.WarehouseRepository;
import com.commerce.platform.warehouse.domain.service.WarehouseDomainService;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePickingTaskHandler 测试")
class CreatePickingTaskHandlerTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private PickingTaskRepository pickingTaskRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private WarehouseDomainService domainService;
    private CreatePickingTaskHandler handler;

    @BeforeEach
    void setUp() {
        domainService = new WarehouseDomainService(warehouseRepository, pickingTaskRepository);
        handler = new CreatePickingTaskHandler(domainService, pickingTaskRepository, eventPublisher);
    }

    @Test
    @DisplayName("处理创建拣货任务命令应保存并发布事件")
    void shouldSaveAndPublishEvent() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main", "Addr");
        warehouse.activate();
        warehouse.setId(1L);
        when(warehouseRepository.findActiveWarehouse()).thenReturn(Optional.of(warehouse));

        when(pickingTaskRepository.save(any(PickingTask.class)))
                .thenAnswer(invocation -> {
                    PickingTask task = invocation.getArgument(0);
                    task.setId(10L);
                    return task;
                });

        CreatePickingTaskCommand command = new CreatePickingTaskCommand(100L);
        PickingTask result = handler.handle(command);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(100L, result.getFulfillmentId());
        assertEquals(1L, result.getWarehouseId());

        verify(pickingTaskRepository).save(any(PickingTask.class));

        ArgumentCaptor<PickingTaskCreatedEvent> captor =
                ArgumentCaptor.forClass(PickingTaskCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        assertEquals(10L, captor.getValue().getPickingTaskId());
        assertEquals(100L, captor.getValue().getFulfillmentId());
    }

    @Test
    @DisplayName("无可用仓库时应抛出异常")
    void shouldThrowExceptionWhenNoWarehouse() {
        when(warehouseRepository.findActiveWarehouse()).thenReturn(Optional.empty());

        CreatePickingTaskCommand command = new CreatePickingTaskCommand(100L);
        assertThrows(IllegalStateException.class, () -> handler.handle(command));

        verify(pickingTaskRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}