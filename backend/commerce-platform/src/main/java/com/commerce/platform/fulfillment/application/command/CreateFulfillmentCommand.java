package com.commerce.platform.fulfillment.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 创建履约单命令
 * <p>
 * 接收来自事件或API的履约单创建请求。
 * 包含创建履约单所需的必要信息。
 * </p>
 */
@Getter
public class CreateFulfillmentCommand {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private final Long orderId;

    /** 商家ID */
    @NotNull(message = "商家ID不能为空")
    private final Long merchantId;

    /**
     * 构造创建履约单命令
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     */
    public CreateFulfillmentCommand(Long orderId, Long merchantId) {
        this.orderId = orderId;
        this.merchantId = merchantId;
    }
}