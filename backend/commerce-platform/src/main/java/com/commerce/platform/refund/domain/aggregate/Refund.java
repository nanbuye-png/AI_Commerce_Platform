package com.commerce.platform.refund.domain.aggregate;

import com.commerce.platform.refund.domain.exception.InvalidRefundStatusException;
import com.commerce.platform.refund.domain.valueobject.RefundReason;
import com.commerce.platform.refund.domain.valueobject.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款聚合根
 * <p>
 * 表示一次退款请求的完整生命周期。
 * 所有状态变更必须通过领域方法完成，禁止外部直接修改 status 字段。
 * </p>
 *
 * <pre>
 * 状态流：
 * REQUESTED → APPROVED → PROCESSING → COMPLETED
 * REQUESTED → REJECTED
 * PROCESSING → FAILED
 * </pre>
 */
public class Refund {

    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private RefundReason reason;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    /**
     * 创建退款申请
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @param amount  退款金额
     * @param reason  退款原因
     * @return 新建的退款（状态为 REQUESTED）
     */
    public static Refund create(Long orderId, Long userId, BigDecimal amount, RefundReason reason) {
        Refund refund = new Refund();
        refund.orderId = orderId;
        refund.userId = userId;
        refund.amount = amount;
        refund.reason = reason;
        refund.status = RefundStatus.REQUESTED;
        refund.createdAt = LocalDateTime.now();
        return refund;
    }

    /**
     * 从持久化恢复退款（全字段构造）
     */
    public static Refund restore(Long id, Long orderId, Long userId, BigDecimal amount,
                                 RefundReason reason, RefundStatus status,
                                 LocalDateTime createdAt, LocalDateTime completedAt) {
        Refund refund = new Refund();
        refund.id = id;
        refund.orderId = orderId;
        refund.userId = userId;
        refund.amount = amount;
        refund.reason = reason;
        refund.status = status;
        refund.createdAt = createdAt;
        refund.completedAt = completedAt;
        return refund;
    }

    // ============================================
    // 领域行为 —— 状态流转
    // ============================================

    /**
     * 审核通过
     * REQUESTED → APPROVED
     */
    public void approve() {
        transitionTo(RefundStatus.APPROVED, "approve");
    }

    /**
     * 开始处理退款
     * APPROVED → PROCESSING
     */
    public void process() {
        transitionTo(RefundStatus.PROCESSING, "process");
    }

    /**
     * 退款完成
     * PROCESSING → COMPLETED
     */
    public void complete() {
        transitionTo(RefundStatus.COMPLETED, "complete");
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 拒绝退款
     * REQUESTED → REJECTED
     */
    public void reject() {
        transitionTo(RefundStatus.REJECTED, "reject");
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 取消退款
     * REQUESTED → REJECTED（取消视为拒绝）
     */
    public void cancel() {
        transitionTo(RefundStatus.REJECTED, "cancel");
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 退款失败
     * PROCESSING → FAILED
     */
    public void fail() {
        transitionTo(RefundStatus.FAILED, "fail");
        this.completedAt = LocalDateTime.now();
    }

    // ============================================
    // 内部状态维护
    // ============================================

    private void transitionTo(RefundStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidRefundStatusException(
                    this.id, this.status.name(), target.name(), operation);
        }
        this.status = target;
    }

    // ============================================
    // Getters & Setters
    // ============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RefundReason getReason() {
        return reason;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}