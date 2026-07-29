package com.commerce.platform.refund.application.command;

/**
 * 完成退款命令
 */
public class CompleteRefundCommand {

    private final Long refundId;

    public CompleteRefundCommand(Long refundId) {
        this.refundId = refundId;
    }

    public Long getRefundId() {
        return refundId;
    }
}