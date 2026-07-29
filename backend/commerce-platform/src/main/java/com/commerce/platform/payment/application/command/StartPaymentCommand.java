package com.commerce.platform.payment.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StartPaymentCommand {
    @NotNull(message = "支付ID不能为空")
    private final Long paymentId;

    public StartPaymentCommand(Long paymentId) {
        this.paymentId = paymentId;
    }
}