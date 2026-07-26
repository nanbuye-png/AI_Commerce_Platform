package com.commerce.platform.product.mq.event;

import lombok.Getter;

/**
 * 商品强制下架事件（预留）
 */
@Getter
public class ProductOffShelfEvent {

    private final Long productId;
    private final Long reviewerId;
    private final String reason;

    public ProductOffShelfEvent(Long productId, Long reviewerId, String reason) {
        this.productId = productId;
        this.reviewerId = reviewerId;
        this.reason = reason;
    }
}