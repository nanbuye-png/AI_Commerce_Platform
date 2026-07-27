package com.commerce.platform.order.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算创建订单请求 DTO
 * <p>
 * 由 CartCheckoutEventListener 组装后传递给 OrderCreationApplicationService。
 * 与 CreateOrderRequest 区分，避免与 OrderDomainService 冲突。
 * </p>
 */
@Data
public class CheckoutCreateOrderRequest {

    private Long userId;
    private Long addressId;
    private List<CheckoutItem> items;

    @Data
    public static class CheckoutItem {
        private Long skuId;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
    }
}