package com.commerce.platform.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<CreateOrderItemRequest> items;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    private String remark;
}