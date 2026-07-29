package com.commerce.platform.returns.application.command;

import com.commerce.platform.returns.domain.valueobject.ReturnReason;

public class CreateReturnCommand {
    private final Long orderId;
    private final Long userId;
    private final ReturnReason reason;

    public CreateReturnCommand(Long orderId, Long userId, ReturnReason reason) {
        this.orderId = orderId; this.userId = userId; this.reason = reason;
    }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public ReturnReason getReason() { return reason; }
}