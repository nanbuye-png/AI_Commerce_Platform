package com.commerce.platform.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户发起收款响应
 */
@Getter
@Builder
public class CreatePaymentResponse {
    private String paymentNo;
    private String orderNo;
    private BigDecimal amount;
    private String qrToken;
    private String qrContent;
    private LocalDateTime expireTime;
}
