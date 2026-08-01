package com.commerce.platform.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求
 * <p>
 * buyerId / merchantId / storeId 不再要求前端传递：
 * buyerId 从 JWT 获取，merchantId / storeId 由后端根据 SKU 推导。
 * </p>
 */
@Data
public class CreateOrderRequest {

    /**
     * 商家 ID（可选，后端根据 SKU 推导）
     */
    private Long merchantId;

    /**
     * 店铺 ID（可选，后端根据 SKU 推导）
     */
    private Long storeId;

    private String remark;

    @NotNull(message = "商品条目不能为空")
    private List<CreateOrderItemRequest> items;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
}