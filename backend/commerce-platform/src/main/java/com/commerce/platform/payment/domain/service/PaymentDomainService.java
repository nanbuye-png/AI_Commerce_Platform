package com.commerce.platform.payment.domain.service;

import com.commerce.platform.payment.domain.aggregate.Payment;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PaymentDomainService {

    /**
     * 创建支付交易
     *
     * @param orderId   订单ID
     * @param userId    用户ID
     * @param amount    支付金额
     * @param paymentNo 支付单号
     * @return 新建的支付聚合
     */
    public Payment createPayment(Long orderId, Long userId, BigDecimal amount, String paymentNo) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0: " + amount);
        }
        if (paymentNo == null || paymentNo.isBlank()) {
            throw new IllegalArgumentException("支付单号不能为空");
        }
        return Payment.create(orderId, userId, amount, paymentNo);
    }
}