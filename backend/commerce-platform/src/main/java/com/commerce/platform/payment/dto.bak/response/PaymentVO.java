package com.commerce.platform.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVO {

    private Long id;
    private String paymentNo;
    private String orderNo;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionNo;
    private LocalDateTime createdTime;
    private LocalDateTime paidTime;
}