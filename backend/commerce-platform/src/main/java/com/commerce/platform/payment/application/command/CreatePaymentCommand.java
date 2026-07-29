package com.commerce.platform.payment.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class CreatePaymentCommand {
    @NotNull(message = "订单ID不能为空")
    private final Long orderId;
    @NotNull(message = "用户ID不能为空")
    private final Long userId;
    @NotNull(message = "支付金额不能为空")
    private final BigDecimal amount;
    @NotNull(message = "支付单号不能为空")
    private final String paymentNo;

    public CreatePaymentCommand(Long orderId, Long userId, BigDecimal amount, String paymentNo) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentNo = paymentNo;
    }
}