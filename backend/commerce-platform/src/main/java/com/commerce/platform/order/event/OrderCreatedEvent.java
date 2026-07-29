package com.commerce.platform.order.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单创建事件
 * <p>
 * Sprint 20 Step 3B: 新增 orderId 字段，统一事件契约。Inventory / Fulfillment Listener
 * 迁移至新架构（ReserveStockHandler 等）时需要 Long 类型的 orderId，而非仅 orderNo(String)。
 * </p>
 */
@Getter
public class OrderCreatedEvent {

    private final Long orderId;
    private final String orderNo;
    private final Long buyerId;
    private final List<OrderItemDto> items;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(Long orderId, String orderNo, Long buyerId, List<OrderItemDto> items) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.items = items;
        this.occurredAt = LocalDateTime.now();
    }

    @Getter
    public static class OrderItemDto {
        private final Long skuId;
        private final Long productId;
        private final Integer quantity;

        public OrderItemDto(Long skuId, Integer quantity) {
            this(skuId, null, quantity);
        }

        /**
         * Sprint 20 Step 4C: 新增 productId，StockReservation Aggregate 需要。
         */
        public OrderItemDto(Long skuId, Long productId, Integer quantity) {
            this.skuId = skuId;
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
