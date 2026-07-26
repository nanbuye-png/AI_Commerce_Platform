package com.commerce.platform.product.mq.event;

import lombok.Getter;

/**
 * 商品审核驳回事件（预留）
 */
@Getter
public class ProductRejectedEvent {

    private final Long productId;
    private final Long reviewerId;
    private final String auditRemark;

    public ProductRejectedEvent(Long productId, Long reviewerId, String auditRemark) {
        this.productId = productId;
        this.reviewerId = reviewerId;
        this.auditRemark = auditRemark;
    }
}