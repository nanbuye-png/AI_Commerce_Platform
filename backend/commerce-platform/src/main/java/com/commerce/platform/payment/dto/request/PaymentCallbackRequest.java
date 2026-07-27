package com.commerce.platform.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付回调请求
 */
@Data
public class PaymentCallbackRequest {

    @NotBlank(message = "交易号不能为空")
    private String transactionNo;
}