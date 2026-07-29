package com.commerce.platform.refund.domain.service;

import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.valueobject.RefundReason;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 退款领域服务
 * <p>
 * 负责创建退款、校验订单状态、协调退款流程。
 * 不直接修改 Refund 状态，状态变化通过 Aggregate 方法。
 * </p>
 */
@Service
public class RefundDomainService {

    /**
     * 创建退款申请
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @param amount  退款金额
     * @param reason  退款原因
     * @return 创建的退款聚合
     */
    public Refund createRefund(Long orderId, Long userId, BigDecimal amount, RefundReason reason) {
        return Refund.create(orderId, userId, amount, reason);
    }
}