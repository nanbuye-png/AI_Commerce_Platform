package com.commerce.platform.payment.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FailPaymentCommand {
    @NotNull(message = "支付ID不能为空")
    private final Long paymentId;
    private final String reason;

    public FailPaymentCommand(Long paymentId, String reason) {
        this.paymentId = paymentId;
        this.reason = reason;
    }
}