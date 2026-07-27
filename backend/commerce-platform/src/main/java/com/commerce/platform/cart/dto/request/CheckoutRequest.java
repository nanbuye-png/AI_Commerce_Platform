package com.commerce.platform.cart.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequest {

    @NotEmpty(message = "购物车商品ID列表不能为空")
    private List<Long> cartItemIds;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotNull(message = "支付方式不能为空")
    private String paymentMethod;
}