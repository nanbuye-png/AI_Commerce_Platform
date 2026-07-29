package com.commerce.platform.returns.application.command;

public class CompleteReturnCommand {
    private final Long returnId;
    private final Long refundId;

    public CompleteReturnCommand(Long returnId, Long refundId) {
        this.returnId = returnId;
        this.refundId = refundId;
    }

    public Long getReturnId() { return returnId; }
    public Long getRefundId() { return refundId; }
}