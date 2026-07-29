package com.commerce.platform.fulfillment.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 履约单创建事件
 * <p>
 * 当履约单成功创建后发布，事件保持不可变。
 * </p>
 */
@Getter
public class FulfillmentCreatedEvent {

    /** 履约单ID */
    private final Long fulfillmentId;

    /** 订单ID */
    private final Long orderId;

    /** 事件发生时间 */
    private final LocalDateTime occurredOn;

    /**
     * 构造履约单创建事件
     *
     * @param fulfillmentId 履约单ID
     * @param orderId       订单ID
     */
    public FulfillmentCreatedEvent(Long fulfillmentId, Long orderId) {
        this.fulfillmentId = fulfillmentId;
        this.orderId = orderId;
        this.occurredOn = LocalDateTime.now();
    }
}