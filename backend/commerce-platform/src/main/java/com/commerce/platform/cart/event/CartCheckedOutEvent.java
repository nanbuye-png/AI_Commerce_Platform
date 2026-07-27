package com.commerce.platform.cart.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车结算事件
 * <p>
 * 当用户结算购物车时发布。
 * 由 CheckoutApplicationService 在创建订单成功后发布。
 * </p>
 */
@Getter
public class CartCheckedOutEvent {

    private final String checkoutNo;
    private final Long cartId;
    private final Long userId;
    private final String orderNo;
    private final List<CartCheckedOutItem> items;
    private final LocalDateTime checkoutTime;

    public CartCheckedOutEvent(String checkoutNo, Long cartId, Long userId, String orderNo, List<CartCheckedOutItem> items) {
        this.checkoutNo = checkoutNo;
        this.cartId = cartId;
        this.userId = userId;
        this.orderNo = orderNo;
        this.items = items;
        this.checkoutTime = LocalDateTime.now();
    }

    @Getter
    public static class CartCheckedOutItem {
        private final Long skuId;
        private final Long productId;
        private final String productName;
        private final BigDecimal price;
        private final Integer quantity;

        public CartCheckedOutItem(Long skuId, Long productId, String productName, BigDecimal price, Integer quantity) {
            this.skuId = skuId;
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }
    }
}