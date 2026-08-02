package com.commerce.platform.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付详情响应（扫码后展示）
 */
@Getter
@Builder
public class PaymentDetailResponse {
    private String paymentNo;
    private String orderNo;
    private BigDecimal amount;
    private LocalDateTime expireTime;
    private String status;
    private String qrToken;
}
