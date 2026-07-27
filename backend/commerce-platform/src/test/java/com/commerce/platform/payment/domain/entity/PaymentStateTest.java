package com.commerce.platform.payment.domain.entity;

import com.commerce.platform.payment.domain.enums.PaymentMethod;
import com.commerce.platform.payment.domain.enums.PaymentStatus;
import com.commerce.platform.payment.exception.InvalidPaymentStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Payment Entity 状态转换覆盖测试
 * <p>
 * 覆盖所有合法路径和非法路径。
 * </p>
 */
class PaymentStateTest {

    private Payment createPayment(PaymentStatus status) {
        return Payment.builder()
                .paymentNo("PAY_TEST_001")
                .orderNo("ORDER_TEST_001")
                .userId(1L)
                .amount(BigDecimal.valueOf(100))
                .paymentMethod(PaymentMethod.MOCK)
                .paymentStatus(status)
                .build();
    }

    // ======== 合法路径 ========

    @Test
    @DisplayName("合法路径：CREATED → PENDING → SUCCESS → REFUNDED")
    void shouldFollowValidFullPath() {
        Payment payment = createPayment(PaymentStatus.CREATED);

        payment.startPay();
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());

        payment.success("TXN_001");
        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        assertEquals("TXN_001", payment.getTransactionNo());
        assertNotNull(payment.getPaidTime());

        payment.refund();
        assertEquals(PaymentStatus.REFUNDED, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("合法路径：CREATED → PENDING → FAILED")
    void shouldAllowPendingToFailed() {
        Payment payment = createPayment(PaymentStatus.PENDING);

        payment.fail();
        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("合法路径：CREATED → PENDING → CLOSED")
    void shouldAllowPendingToClosed() {
        Payment payment = createPayment(PaymentStatus.PENDING);

        payment.close();
        assertEquals(PaymentStatus.CLOSED, payment.getPaymentStatus());
    }

    // ======== 非法路径 ========

    @Test
    @DisplayName("非法路径：CREATED.refund() 应抛出异常")
    void shouldNotAllowCreatedToRefund() {
        Payment payment = createPayment(PaymentStatus.CREATED);
        assertThrows(InvalidPaymentStatusException.class, payment::refund);
    }

    @Test
    @DisplayName("非法路径：SUCCESS.success() 应抛出异常")
    void shouldNotAllowSuccessToSuccess() {
        Payment payment = createPayment(PaymentStatus.SUCCESS);
        assertThrows(InvalidPaymentStatusException.class, () -> payment.success("TXN_002"));
    }

    @Test
    @DisplayName("非法路径：FAILED.success() 应抛出异常")
    void shouldNotAllowFailedToSuccess() {
        Payment payment = createPayment(PaymentStatus.FAILED);
        assertThrows(InvalidPaymentStatusException.class, () -> payment.success("TXN_003"));
    }

    @Test
    @DisplayName("非法路径：SUCCESS.fail() 应抛出异常")
    void shouldNotAllowSuccessToFail() {
        Payment payment = createPayment(PaymentStatus.SUCCESS);
        assertThrows(InvalidPaymentStatusException.class, payment::fail);
    }

    @Test
    @DisplayName("非法路径：CLOSED.startPay() 应抛出异常")
    void shouldNotAllowClosedToStartPay() {
        Payment payment = createPayment(PaymentStatus.CLOSED);
        assertThrows(InvalidPaymentStatusException.class, payment::startPay);
    }

    @Test
    @DisplayName("非法路径：REFUNDED.startPay() 应抛出异常")
    void shouldNotAllowRefundedToStartPay() {
        Payment payment = createPayment(PaymentStatus.REFUNDED);
        assertThrows(InvalidPaymentStatusException.class, payment::startPay);
    }
}