package com.commerce.platform.payment.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单创建待支付事件
 * <p>
 * 由 Order Creation 流程发布，Payment Domain 监听并创建支付记录。
 * Order Domain 不依赖 Payment Domain，通过 Spring Event 解耦。
 * </p>
 */
@Getter
public class OrderCreatedForPaymentEvent {

    private final String orderNo;
    private final Long userId;
    private final BigDecimal amount;
    private final String checkoutNo;
    private final LocalDateTime createTime;

    public OrderCreatedForPaymentEvent(String orderNo, Long userId, BigDecimal amount, String checkoutNo) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.amount = amount;
        this.checkoutNo = checkoutNo;
        this.createTime = LocalDateTime.now();
    }
}