package com.commerce.platform.payment.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CompletePaymentCommand {
    @NotNull(message = "支付ID不能为空")
    private final Long paymentId;
    @NotNull(message = "交易流水号不能为空")
    private final String transactionNo;

    public CompletePaymentCommand(Long paymentId, String transactionNo) {
        this.paymentId = paymentId;
        this.transactionNo = transactionNo;
    }
}