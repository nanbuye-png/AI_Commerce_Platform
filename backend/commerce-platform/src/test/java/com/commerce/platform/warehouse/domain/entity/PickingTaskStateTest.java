package com.commerce.platform.warehouse.domain.entity;

import com.commerce.platform.warehouse.domain.aggregate.PickingTask;
import com.commerce.platform.warehouse.domain.exception.InvalidTaskStatusException;
import com.commerce.platform.warehouse.domain.valueobject.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PickingTask 状态流转测试")
class PickingTaskStateTest {

    private PickingTask task;

    @BeforeEach
    void setUp() {
        task = PickingTask.create(100L, 50L);
    }

    @Test
    @DisplayName("创建任务应初始化为 CREATED")
    void shouldBeCreatedWhenCreated() {
        assertEquals(TaskStatus.CREATED, task.getStatus());
        assertEquals(100L, task.getFulfillmentId());
        assertEquals(50L, task.getWarehouseId());
    }

    @Test
    @DisplayName("CREATED → PROCESSING → COMPLETED")
    void shouldCompleteNormalFlow() {
        task.startPicking();
        assertEquals(TaskStatus.PROCESSING, task.getStatus());

        task.completePicking();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    @DisplayName("CREATED → PROCESSING → FAILED")
    void shouldFailFromProcessing() {
        task.startPicking();
        task.failPicking();
        assertEquals(TaskStatus.FAILED, task.getStatus());
    }

    @Test
    @DisplayName("CREATED → CANCELLED")
    void shouldCancelFromCreated() {
        task.cancel();
        assertEquals(TaskStatus.CANCELLED, task.getStatus());
    }

    @Test
    @DisplayName("PROCESSING → CANCELLED")
    void shouldCancelFromProcessing() {
        task.startPicking();
        task.cancel();
        assertEquals(TaskStatus.CANCELLED, task.getStatus());
    }

    @Test
    @DisplayName("COMPLETED 不可再流转")
    void shouldNotTransitionFromCompleted() {
        task.startPicking();
        task.completePicking();

        assertThrows(InvalidTaskStatusException.class, () -> task.startPicking());
        assertThrows(InvalidTaskStatusException.class, () -> task.cancel());
    }

    @Test
    @DisplayName("CREATED → COMPLETED 非法跳转")
    void shouldNotAllowCreatedToCompleted() {
        assertThrows(InvalidTaskStatusException.class, () -> task.completePicking());
    }

    @Test
    @DisplayName("CANCELLED 不可再流转")
    void shouldNotTransitionFromCancelled() {
        task.cancel();
        assertThrows(InvalidTaskStatusException.class, () -> task.startPicking());
    }
}