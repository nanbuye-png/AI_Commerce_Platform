package com.commerce.platform.refund.payment;

import java.math.BigDecimal;

/**
 * 支付退款网关抽象接口
 * <p>
 * 定义退款网关的契约，属于 Domain 层的 Port（入站端口）。
 * 具体支付适配器（WechatRefundAdapter、AlipayRefundAdapter、StripeRefundAdapter）
 * 在 Infrastructure 层实现此接口。
 * </p>
 */
public interface PaymentRefundGateway {

    /**
     * 执行退款
     *
     * @param orderNo  订单号
     * @param amount   退款金额
     * @param refundNo 退款单号
     * @return true 如果退款成功
     */
    boolean refund(String orderNo, BigDecimal amount, String refundNo);
}