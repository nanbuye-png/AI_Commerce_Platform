package com.commerce.platform.payment.domain.aggregate;

import com.commerce.platform.payment.domain.exception.InvalidPaymentStatusException;
import com.commerce.platform.payment.domain.valueobject.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付聚合根
 * <p>
 * 表示一次支付交易的完整生命周期，聚合根负责维护自身状态。
 * 所有状态变更必须通过领域方法完成，禁止外部直接修改字段。
 * </p>
 *
 * <pre>
 * 状态流：
 * CREATED
 *   ↓
 * PROCESSING
 *   ↓
 * PAID
 *
 * CREATED  → CANCELLED
 * CREATED  → FAILED
 * PROCESSING → FAILED
 * </pre>
 */
public class Payment {

    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentNo;
    private String transactionNo;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime failedAt;

    /**
     * 创建新的支付交易
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @param amount  支付金额
     * @param paymentNo 支付单号
     * @return 新建的支付交易（状态为 CREATED）
     */
    public static Payment create(Long orderId, Long userId, BigDecimal amount, String paymentNo) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.userId = userId;
        payment.amount = amount;
        payment.paymentNo = paymentNo;
        payment.status = PaymentStatus.CREATED;
        payment.createdAt = LocalDateTime.now();
        return payment;
    }

    /**
     * 从持久化恢复支付交易（全字段构造）
     */
    public static Payment restore(Long id, Long orderId, Long userId, BigDecimal amount,
                                   PaymentStatus status, String paymentNo, String transactionNo,
                                   LocalDateTime createdAt, LocalDateTime paidAt, LocalDateTime failedAt) {
        Payment payment = new Payment();
        payment.id = id;
        payment.orderId = orderId;
        payment.userId = userId;
        payment.amount = amount;
        payment.status = status;
        payment.paymentNo = paymentNo;
        payment.transactionNo = transactionNo;
        payment.createdAt = createdAt;
        payment.paidAt = paidAt;
        payment.failedAt = failedAt;
        return payment;
    }

    // ============================================
    // 领域行为 —— 状态流转（由 Aggregate 自身维护）
    // ============================================

    /**
     * 开始处理支付
     * <p>
     * CREATED → PROCESSING
     * </p>
     */
    public void startProcessing() {
        transitionTo(PaymentStatus.PROCESSING, "startProcessing");
    }

    /**
     * 标记支付成功
     * <p>
     * PROCESSING → PAID
     * </p>
     *
     * @param transactionNo 交易流水号
     */
    public void markPaid(String transactionNo) {
        transitionTo(PaymentStatus.PAID, "markPaid");
        this.transactionNo = transactionNo;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 标记支付失败
     * <p>
     * CREATED/PROCESSING → FAILED
     * </p>
     */
    public void fail() {
        transitionTo(PaymentStatus.FAILED, "fail");
        this.failedAt = LocalDateTime.now();
    }

    /**
     * 取消支付
     * <p>
     * CREATED → CANCELLED
     * </p>
     */
    public void cancel() {
        transitionTo(PaymentStatus.CANCELLED, "cancel");
    }

    // ============================================
    // 内部状态维护
    // ============================================

    private void transitionTo(PaymentStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidPaymentStatusException(
                    this.id, this.status.name(), target.name(), operation);
        }
        this.status = target;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ============================================
    // Getters
    // ============================================

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentNo() { return paymentNo; }
    public String getTransactionNo() { return transactionNo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getFailedAt() { return failedAt; }
}