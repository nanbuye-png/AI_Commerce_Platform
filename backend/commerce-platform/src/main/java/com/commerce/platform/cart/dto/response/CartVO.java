package com.commerce.platform.cart.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CartVO {
    private Long id;
    private Long userId;
    private List<CartItemVO> items;
}