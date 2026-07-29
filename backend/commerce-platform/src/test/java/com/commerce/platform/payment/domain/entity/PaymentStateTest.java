package com.commerce.platform.payment.domain.entity;

import com.commerce.platform.payment.domain.aggregate.Payment;
import com.commerce.platform.payment.domain.exception.InvalidPaymentStatusException;
import com.commerce.platform.payment.domain.valueobject.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment 状态流转测试")
class PaymentStateTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.create(1L, 100L, new BigDecimal("99.99"), "PAY20250101001");
    }

    @Test @DisplayName("创建支付应初始化为 CREATED")
    void shouldBeCreatedWhenCreated() {
        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        assertEquals(1L, payment.getOrderId());
        assertEquals(100L, payment.getUserId());
        assertEquals(new BigDecimal("99.99"), payment.getAmount());
    }

    @Test @DisplayName("CREATED → PROCESSING")
    void shouldTransitionToProcessing() {
        payment.startProcessing();
        assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
    }

    @Test @DisplayName("PROCESSING → PAID")
    void shouldTransitionToPaid() {
        payment.startProcessing();
        payment.markPaid("TXN001");
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals("TXN001", payment.getTransactionNo());
        assertNotNull(payment.getPaidAt());
    }

    @Test @DisplayName("PROCESSING → FAILED")
    void shouldTransitionToFailed() {
        payment.startProcessing();
        payment.fail();
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertNotNull(payment.getFailedAt());
    }

    @Test @DisplayName("CREATED → CANCELLED")
    void shouldAllowCancelFromCreated() {
        payment.cancel();
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
    }

    @Test @DisplayName("PAID 不可继续流转")
    void shouldNotTransitionFromPaid() {
        payment.startProcessing();
        payment.markPaid("TXN001");
        assertThrows(InvalidPaymentStatusException.class, () -> payment.startProcessing());
        assertThrows(InvalidPaymentStatusException.class, () -> payment.fail());
        assertThrows(InvalidPaymentStatusException.class, () -> payment.cancel());
    }

    @Test @DisplayName("FAILED 不可继续流转")
    void shouldNotTransitionFromFailed() {
        payment.startProcessing();
        payment.fail();
        assertThrows(InvalidPaymentStatusException.class, () -> payment.markPaid("TXN002"));
        assertThrows(InvalidPaymentStatusException.class, () -> payment.cancel());
    }

    @Test @DisplayName("CANCELLED 不可继续流转")
    void shouldNotTransitionFromCancelled() {
        payment.cancel();
        assertThrows(InvalidPaymentStatusException.class, () -> payment.startProcessing());
        assertThrows(InvalidPaymentStatusException.class, () -> payment.fail());
    }

    @Test @DisplayName("CREATED → PAID 直接跳转非法")
    void shouldThrowExceptionForCreatedToPaid() {
        assertThrows(InvalidPaymentStatusException.class, () -> payment.markPaid("TXN003"));
    }

    @Test @DisplayName("restore 应正确恢复")
    void shouldRestoreAllFields() {
        payment.startProcessing();
        payment.markPaid("TXN004");
        Payment restored = Payment.restore(1L, 1L, 100L, new BigDecimal("99.99"),
                PaymentStatus.PAID, "PAY001", "TXN004",
                payment.getCreatedAt(), payment.getPaidAt(), null);
        assertEquals(PaymentStatus.PAID, restored.getStatus());
        assertEquals("TXN004", restored.getTransactionNo());
    }
}