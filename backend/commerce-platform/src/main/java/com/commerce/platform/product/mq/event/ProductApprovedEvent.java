package com.commerce.platform.product.mq.event;

import lombok.Getter;

/**
 * 商品审核通过事件（预留）
 */
@Getter
public class ProductApprovedEvent {

    private final Long productId;
    private final Long reviewerId;
    private final String auditRemark;

    public ProductApprovedEvent(Long productId, Long reviewerId, String auditRemark) {
        this.productId = productId;
        this.reviewerId = reviewerId;
        this.auditRemark = auditRemark;
    }
}