package com.commerce.platform.refund.domain.exception;

/**
 * 非法退款状态迁移异常
 */
public class InvalidRefundStatusException extends RuntimeException {

    private final Long refundId;
    private final String currentStatus;
    private final String targetStatus;
    private final String operation;

    public InvalidRefundStatusException(Long refundId, String currentStatus, String targetStatus, String operation) {
        super(String.format("退款状态迁移非法: refundId=%d, 当前=%s, 目标=%s, 操作=%s",
                refundId, currentStatus, targetStatus, operation));
        this.refundId = refundId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
        this.operation = operation;
    }

    public Long getRefundId() {
        return refundId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public String getOperation() {
        return operation;
    }
}