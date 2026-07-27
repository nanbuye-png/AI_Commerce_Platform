package com.commerce.platform.order.service.compensation;

/**
 * 优惠券回滚服务接口（预留）
 * <p>
 * 当订单取消或关闭时，需要归还已使用的优惠券。
 * 完整实现将在后续 Sprint 中完成。
 * </p>
 */
public interface CouponRollbackService {

    /**
     * 归还订单使用的优惠券
     *
     * @param orderNo 订单号
     */
    void restoreCoupon(String orderNo);
}