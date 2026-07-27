package com.commerce.platform.order.dto.request;

import lombok.Data;

/**
 * Admin 强制关闭订单请求
 */
@Data
public class AdminCloseOrderRequest {

    /**
     * 关闭原因
     */
    private String closeReason;
}