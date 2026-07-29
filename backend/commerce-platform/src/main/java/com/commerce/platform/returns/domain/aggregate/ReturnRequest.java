package com.commerce.platform.returns.domain.aggregate;

import com.commerce.platform.returns.domain.exception.InvalidReturnStatusException;
import com.commerce.platform.returns.domain.valueobject.ReturnReason;
import com.commerce.platform.returns.domain.valueobject.ReturnStatus;

import java.time.LocalDateTime;

/**
 * 退货聚合根
 * <p>
 * 表示一次退货申请的完整生命周期。
 * 所有状态变更必须通过领域方法完成，禁止外部直接修改 status 字段。
 * </p>
 */
public class ReturnRequest {

    private Long id;
    private Long orderId;
    private Long userId;
    private Long refundId;
    private ReturnReason reason;
    private ReturnStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime completedAt;

    public static ReturnRequest create(Long orderId, Long userId, ReturnReason reason) {
        ReturnRequest request = new ReturnRequest();
        request.orderId = orderId;
        request.userId = userId;
        request.reason = reason;
        request.status = ReturnStatus.REQUESTED;
        request.createdAt = LocalDateTime.now();
        return request;
    }

    public static ReturnRequest restore(Long id, Long orderId, Long userId, Long refundId,
                                         ReturnReason reason, ReturnStatus status,
                                         LocalDateTime createdAt, LocalDateTime approvedAt,
                                         LocalDateTime completedAt) {
        ReturnRequest request = new ReturnRequest();
        request.id = id;
        request.orderId = orderId;
        request.userId = userId;
        request.refundId = refundId;
        request.reason = reason;
        request.status = status;
        request.createdAt = createdAt;
        request.approvedAt = approvedAt;
        request.completedAt = completedAt;
        return request;
    }

    public void approve() { transitionTo(ReturnStatus.APPROVED, "approve"); this.approvedAt = LocalDateTime.now(); }
    public void reject() { transitionTo(ReturnStatus.REJECTED, "reject"); this.completedAt = LocalDateTime.now(); }
    public void beginReturn() { transitionTo(ReturnStatus.RETURNING, "beginReturn"); }
    public void receive() { transitionTo(ReturnStatus.RECEIVED, "receive"); }
    public void complete() { transitionTo(ReturnStatus.COMPLETED, "complete"); this.completedAt = LocalDateTime.now(); }
    public void cancel() { transitionTo(ReturnStatus.REJECTED, "cancel"); this.completedAt = LocalDateTime.now(); }
    public void fail() { transitionTo(ReturnStatus.FAILED, "fail"); this.completedAt = LocalDateTime.now(); }

    private void transitionTo(ReturnStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidReturnStatusException(this.id, this.status.name(), target.name(), operation);
        }
        this.status = target;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public Long getRefundId() { return refundId; }
    public void setRefundId(Long refundId) { this.refundId = refundId; }
    public ReturnReason getReason() { return reason; }
    public ReturnStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}