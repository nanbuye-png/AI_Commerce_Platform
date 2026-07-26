package com.commerce.platform.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发货请求
 */
@Data
public class ShipOrderRequest {

    @NotBlank(message = "物流单号不能为空")
    private String trackingNo;

    @NotBlank(message = "物流公司不能为空")
    private String logisticsCompany;
}