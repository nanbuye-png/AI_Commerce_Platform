package com.commerce.platform.warehouse.domain.entity;

import com.commerce.platform.warehouse.domain.aggregate.PackingTask;
import com.commerce.platform.warehouse.domain.exception.InvalidTaskStatusException;
import com.commerce.platform.warehouse.domain.valueobject.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PackingTask 状态流转测试")
class PackingTaskStateTest {

    private PackingTask task;

    @BeforeEach
    void setUp() {
        task = PackingTask.create(100L, 50L);
    }

    @Test
    @DisplayName("创建打包任务应初始化为 CREATED")
    void shouldBeCreatedWhenCreated() {
        assertEquals(TaskStatus.CREATED, task.getStatus());
        assertEquals(100L, task.getFulfillmentId());
        assertEquals(50L, task.getPickingTaskId());
    }

    @Test
    @DisplayName("CREATED → PROCESSING → COMPLETED 正常打包")
    void shouldCompletePacking() {
        task.startPacking();
        assertEquals(TaskStatus.PROCESSING, task.getStatus());

        task.completePacking();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getPackedAt());
    }

    @Test
    @DisplayName("CREATED → PROCESSING → FAILED")
    void shouldFailPacking() {
        task.startPacking();
        task.failPacking();
        assertEquals(TaskStatus.FAILED, task.getStatus());
    }

    @Test
    @DisplayName("CREATED → COMPLETED 非法跳转")
    void shouldNotAllowCreatedToCompleted() {
        assertThrows(InvalidTaskStatusException.class, () -> task.completePacking());
    }

    @Test
    @DisplayName("COMPLETED 不可再流转")
    void shouldNotTransitionFromCompleted() {
        task.startPacking();
        task.completePacking();

        assertThrows(InvalidTaskStatusException.class, () -> task.startPacking());
        assertThrows(InvalidTaskStatusException.class, () -> task.failPacking());
    }

    @Test
    @DisplayName("FAILED 不可再流转")
    void shouldNotTransitionFromFailed() {
        task.startPacking();
        task.failPacking();

        assertThrows(InvalidTaskStatusException.class, () -> task.startPacking());
    }
}