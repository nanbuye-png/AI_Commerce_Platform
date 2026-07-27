package com.commerce.platform.payment.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.payment.domain.enums.PaymentMethod;
import com.commerce.platform.payment.domain.enums.PaymentStatus;
import com.commerce.platform.payment.exception.InvalidPaymentStatusException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付实体
 * Payment Domain 的聚合根。
 * <p>
 * 所有状态变化必须通过 Entity 方法，禁止外部直接 setPaymentStatus()。
 * </p>
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_no", columnList = "payment_no", unique = true),
    @Index(name = "idx_order_no", columnList = "order_no"),
    @Index(name = "idx_transaction_no", columnList = "transaction_no")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(name = "payment_no", nullable = false, unique = true, length = 32, updatable = false)
    private String paymentNo;

    @Column(name = "order_no", nullable = false, length = 32, updatable = false)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.CREATED;

    @Column(name = "transaction_no", length = 64)
    private String transactionNo;

    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    // ============================================
    // 领域行为 —— 状态流转（由 Entity 自身维护）
    // ============================================

    /**
     * 发起支付
     * CREATED → PENDING
     */
    public void startPay() {
        assertValidStatus(PaymentStatus.CREATED, "发起支付");
        this.paymentStatus = PaymentStatus.PENDING;
    }

    /**
     * 支付成功
     * PENDING → SUCCESS
     */
    public void success(String transactionNo) {
        assertValidStatus(PaymentStatus.PENDING, "支付成功");
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.transactionNo = transactionNo;
        this.paidTime = LocalDateTime.now();
    }

    /**
     * 支付失败
     * PENDING → FAILED
     */
    public void fail() {
        assertValidStatus(PaymentStatus.PENDING, "支付失败");
        this.paymentStatus = PaymentStatus.FAILED;
    }

    /**
     * 关闭支付（PENDING 超时等）
     * PENDING → CLOSED
     */
    public void close() {
        assertValidStatus(PaymentStatus.PENDING, "关闭");
        this.paymentStatus = PaymentStatus.CLOSED;
    }

    /**
     * 退款
     * SUCCESS → REFUNDED
     */
    public void refund() {
        assertValidStatus(PaymentStatus.SUCCESS, "退款");
        this.paymentStatus = PaymentStatus.REFUNDED;
    }

    // ============================================
    // 辅助方法
    // ============================================

    private void assertValidStatus(PaymentStatus expected, String operation) {
        if (this.paymentStatus != expected) {
            throw new InvalidPaymentStatusException(this.paymentNo, this.paymentStatus.name(), operation);
        }
    }
}