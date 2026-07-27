package com.commerce.platform.payment.provider;

import com.commerce.platform.payment.domain.entity.Payment;
import com.commerce.platform.payment.provider.model.PaymentResult;
import com.commerce.platform.payment.provider.model.RefundResult;

/**
 * 支付提供者接口
 * <p>
 * 屏蔽第三方支付差异，支持多种支付方式接入。
 * Provider 不依赖 Order Entity，只处理支付。
 * </p>
 */
public interface PaymentProvider {

    /**
     * 发起支付请求
     */
    PaymentResult pay(Payment payment);

    /**
     * 查询支付结果
     */
    PaymentResult query(String transactionNo);

    /**
     * 发起退款
     */
    RefundResult refund(Payment payment);
}