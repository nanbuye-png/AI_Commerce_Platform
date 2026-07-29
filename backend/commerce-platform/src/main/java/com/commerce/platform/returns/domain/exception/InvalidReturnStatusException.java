package com.commerce.platform.returns.domain.exception;

public class InvalidReturnStatusException extends RuntimeException {
    private final Long returnId;
    private final String currentStatus;
    private final String targetStatus;
    private final String operation;

    public InvalidReturnStatusException(Long returnId, String currentStatus, String targetStatus, String operation) {
        super(String.format("退货状态迁移非法: returnId=%d, 当前=%s, 目标=%s, 操作=%s",
                returnId, currentStatus, targetStatus, operation));
        this.returnId = returnId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
        this.operation = operation;
    }

    public Long getReturnId() { return returnId; }
    public String getCurrentStatus() { return currentStatus; }
    public String getTargetStatus() { return targetStatus; }
    public String getOperation() { return operation; }
}