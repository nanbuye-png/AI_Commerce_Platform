package com.commerce.platform.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Admin 强制取消订单请求
 */
@Data
public class AdminCancelOrderRequest {

    /**
     * 取消原因
     */
    @NotBlank(message = "取消原因不能为空")
    private String cancelReason;
}