package com.commerce.platform.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultVO {

    private String paymentNo;
    private String paymentStatus;
    private String transactionNo;
}