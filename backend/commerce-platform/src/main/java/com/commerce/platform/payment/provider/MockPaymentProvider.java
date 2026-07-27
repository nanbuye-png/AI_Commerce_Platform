package com.commerce.platform.payment.provider;

import com.commerce.platform.payment.domain.entity.Payment;
import com.commerce.platform.payment.provider.model.PaymentResult;
import com.commerce.platform.payment.provider.model.RefundResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock 支付提供者
 * <p>
 * 模拟第三方支付，固定返回成功。
 * 用于开发/测试环境。
 * </p>
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult pay(Payment payment) {
        String transactionNo = "MOCK_TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return PaymentResult.builder()
                .success(true)
                .transactionNo(transactionNo)
                .message("Mock payment successful")
                .build();
    }

    @Override
    public PaymentResult query(String transactionNo) {
        return PaymentResult.builder()
                .success(true)
                .transactionNo(transactionNo)
                .message("Mock payment query successful")
                .build();
    }

    @Override
    public RefundResult refund(Payment payment) {
        String refundNo = "MOCK_REF_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return RefundResult.builder()
                .success(true)
                .refundNo(refundNo)
                .message("Mock refund successful")
                .build();
    }
}