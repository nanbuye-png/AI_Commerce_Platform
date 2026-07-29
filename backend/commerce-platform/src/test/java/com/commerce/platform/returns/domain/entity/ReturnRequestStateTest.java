package com.commerce.platform.returns.domain.entity;

import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.exception.InvalidReturnStatusException;
import com.commerce.platform.returns.domain.valueobject.ReturnReason;
import com.commerce.platform.returns.domain.valueobject.ReturnStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReturnRequest 状态流转测试")
class ReturnRequestStateTest {

    @Test @DisplayName("创建退货应初始化为 REQUESTED")
    void shouldBeRequestedWhenCreated() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.QUALITY_ISSUE);
        assertEquals(ReturnStatus.REQUESTED, r.getStatus());
    }

    @Test @DisplayName("完整流程: REQUESTED→APPROVED→RETURNING→RECEIVED→COMPLETED")
    void shouldTransitionThroughAllValidStates() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.QUALITY_ISSUE);
        r.approve(); assertEquals(ReturnStatus.APPROVED, r.getStatus()); assertNotNull(r.getApprovedAt());
        r.beginReturn(); assertEquals(ReturnStatus.RETURNING, r.getStatus());
        r.receive(); assertEquals(ReturnStatus.RECEIVED, r.getStatus());
        r.complete(); assertEquals(ReturnStatus.COMPLETED, r.getStatus()); assertNotNull(r.getCompletedAt());
    }

    @Test @DisplayName("拒绝流程: REQUESTED → REJECTED")
    void shouldTransitionToRejected() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        r.reject(); assertEquals(ReturnStatus.REJECTED, r.getStatus()); assertNotNull(r.getCompletedAt());
    }

    @Test @DisplayName("取消视为 REJECTED")
    void shouldTransitionToRejectedOnCancel() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        r.cancel(); assertEquals(ReturnStatus.REJECTED, r.getStatus()); assertNotNull(r.getCompletedAt());
    }

    @Test @DisplayName("RETURNING → FAILED")
    void shouldTransitionToFailed() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        r.approve(); r.beginReturn();
        r.fail(); assertEquals(ReturnStatus.FAILED, r.getStatus()); assertNotNull(r.getCompletedAt());
    }

    @Test @DisplayName("COMPLETED 不可继续流转")
    void shouldNotTransitionFromCompleted() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        r.approve(); r.beginReturn(); r.receive(); r.complete();
        assertThrows(InvalidReturnStatusException.class, r::approve);
        assertThrows(InvalidReturnStatusException.class, r::beginReturn);
        assertThrows(InvalidReturnStatusException.class, r::receive);
        assertThrows(InvalidReturnStatusException.class, r::complete);
        assertThrows(InvalidReturnStatusException.class, r::reject);
    }

    @Test @DisplayName("REJECTED 不可继续流转")
    void shouldNotTransitionFromRejected() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        r.reject();
        assertThrows(InvalidReturnStatusException.class, r::approve);
    }

    @Test @DisplayName("REQUESTED → RETURNING 应抛异常")
    void shouldThrowForRequestedToReturning() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        assertThrows(InvalidReturnStatusException.class, r::beginReturn);
    }

    @Test @DisplayName("APPROVED → COMPLETED 应抛异常")
    void shouldThrowForApprovedToCompleted() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        r.approve();
        assertThrows(InvalidReturnStatusException.class, r::complete);
    }

    @Test @DisplayName("RECEIVED → APPROVED 应抛异常（回退）")
    void shouldThrowForReceivedBackToApproved() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.OTHER);
        r.approve(); r.beginReturn(); r.receive();
        assertThrows(InvalidReturnStatusException.class, r::approve);
    }
}