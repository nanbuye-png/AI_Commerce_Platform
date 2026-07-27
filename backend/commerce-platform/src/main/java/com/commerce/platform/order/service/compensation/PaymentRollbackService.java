package com.commerce.platform.order.service.compensation;

/**
 * 支付回滚服务接口（预留）
 * <p>
 * 当订单关闭或退款时，需要执行支付退款流程。
 * 完整实现将在 Payment Domain 接入时完成。
 * </p>
 */
public interface PaymentRollbackService {

    /**
     * 退款
     *
     * @param orderNo 订单号
     */
    void refund(String orderNo);
}