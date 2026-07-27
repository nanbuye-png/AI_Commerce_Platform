package com.commerce.platform.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "买家ID不能为空")
    private Long buyerId;

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotNull(message = "店铺ID不能为空")
    private Long storeId;

    private String remark;

    @NotNull(message = "商品条目不能为空")
    private List<CreateOrderItemRequest> items;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
}
