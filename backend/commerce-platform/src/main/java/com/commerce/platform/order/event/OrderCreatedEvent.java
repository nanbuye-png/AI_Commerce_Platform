package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单创建事件
 */
@Getter
public class OrderCreatedEvent {

    private final String orderNo;
    private final Long buyerId;
    private final List<OrderItemDto> items;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(String orderNo, Long buyerId, List<OrderItemDto> items) {
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.items = items;
        this.occurredAt = LocalDateTime.now();
    }

    @Getter
    public static class OrderItemDto {
        private final Long skuId;
        private final Integer quantity;

        public OrderItemDto(Long skuId, Integer quantity) {
            this.skuId = skuId;
            this.quantity = quantity;
        }
    }
}
