package com.commerce.platform.refund.application.command;

import com.commerce.platform.refund.domain.valueobject.RefundReason;

import java.math.BigDecimal;

/**
 * 创建退款命令
 */
public class CreateRefundCommand {

    private final Long orderId;
    private final Long userId;
    private final BigDecimal amount;
    private final RefundReason reason;

    public CreateRefundCommand(Long orderId, Long userId, BigDecimal amount, RefundReason reason) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
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
}