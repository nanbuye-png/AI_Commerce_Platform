package com.commerce.platform.refund.domain.entity;

import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.exception.InvalidRefundStatusException;
import com.commerce.platform.refund.domain.valueobject.RefundReason;
import com.commerce.platform.refund.domain.valueobject.RefundStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Refund 状态流转测试
 */
@DisplayName("Refund 状态流转测试")
class RefundStateTest {

    @Test
    @DisplayName("创建退款应初始化为 REQUESTED 状态")
    void shouldBeRequestedWhenCreated() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.QUALITY_ISSUE);
        assertEquals(RefundStatus.REQUESTED, refund.getStatus());
        assertEquals(1L, refund.getOrderId());
        assertEquals(100L, refund.getUserId());
        assertEquals(new BigDecimal("99.99"), refund.getAmount());
        assertEquals(RefundReason.QUALITY_ISSUE, refund.getReason());
    }

    @Test
    @DisplayName("完整正常流程：REQUESTED → APPROVED → PROCESSING → COMPLETED")
    void shouldTransitionThroughAllValidStates() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.QUALITY_ISSUE);

        refund.approve();
        assertEquals(RefundStatus.APPROVED, refund.getStatus());

        refund.process();
        assertEquals(RefundStatus.PROCESSING, refund.getStatus());

        refund.complete();
        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        assertNotNull(refund.getCompletedAt());
    }

    @Test
    @DisplayName("拒绝流程：REQUESTED → REJECTED")
    void shouldTransitionToRejected() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);

        refund.reject();
        assertEquals(RefundStatus.REJECTED, refund.getStatus());
        assertNotNull(refund.getCompletedAt());
    }

    @Test
    @DisplayName("取消流程：REQUESTED → REJECTED（取消视为拒绝）")
    void shouldTransitionToRejectedOnCancel() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);

        refund.cancel();
        assertEquals(RefundStatus.REJECTED, refund.getStatus());
        assertNotNull(refund.getCompletedAt());
    }

    @Test
    @DisplayName("失败流程：PROCESSING → FAILED")
    void shouldTransitionToFailed() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);
        refund.approve();
        refund.process();

        refund.fail();
        assertEquals(RefundStatus.FAILED, refund.getStatus());
        assertNotNull(refund.getCompletedAt());
    }

    @Test
    @DisplayName("COMPLETED 状态不可继续流转")
    void shouldNotTransitionFromCompleted() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);
        refund.approve();
        refund.process();
        refund.complete();

        assertThrows(InvalidRefundStatusException.class, refund::approve);
        assertThrows(InvalidRefundStatusException.class, refund::process);
        assertThrows(InvalidRefundStatusException.class, refund::complete);
        assertThrows(InvalidRefundStatusException.class, refund::reject);
        assertThrows(InvalidRefundStatusException.class, refund::cancel);
        assertThrows(InvalidRefundStatusException.class, refund::fail);
    }

    @Test
    @DisplayName("REJECTED 状态不可继续流转")
    void shouldNotTransitionFromRejected() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);
        refund.reject();

        assertThrows(InvalidRefundStatusException.class, refund::approve);
        assertThrows(InvalidRefundStatusException.class, refund::process);
        assertThrows(InvalidRefundStatusException.class, refund::complete);
    }

    @Test
    @DisplayName("FAILED 状态不可继续流转")
    void shouldNotTransitionFromFailed() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);
        refund.approve();
        refund.process();
        refund.fail();

        assertThrows(InvalidRefundStatusException.class, refund::approve);
        assertThrows(InvalidRefundStatusException.class, refund::process);
        assertThrows(InvalidRefundStatusException.class, refund::complete);
        assertThrows(InvalidRefundStatusException.class, refund::fail);
    }

    @Test
    @DisplayName("REQUESTED → PROCESSING 应抛异常（跳过 APPROVED）")
    void shouldThrowExceptionForRequestedToProcessing() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);
        assertThrows(InvalidRefundStatusException.class, refund::process);
    }

    @Test
    @DisplayName("APPROVED → COMPLETED 应抛异常（跳过 PROCESSING）")
    void shouldThrowExceptionForApprovedToCompleted() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);
        refund.approve();
        assertThrows(InvalidRefundStatusException.class, refund::complete);
    }

    @Test
    @DisplayName("REQUESTED → FAILED 应抛异常")
    void shouldThrowExceptionForRequestedToFailed() {
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.OTHER);
        assertThrows(InvalidRefundStatusException.class, refund::fail);
    }
}