package com.commerce.platform.payment.dto.request;

import com.commerce.platform.payment.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付请求
 */
@Data
public class CreatePaymentRequest {

    @NotNull(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "支付方式不能为空")
    private PaymentMethod paymentMethod;
}