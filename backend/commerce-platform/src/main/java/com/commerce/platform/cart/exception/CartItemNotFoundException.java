package com.commerce.platform.cart.exception;

public class CartItemNotFoundException extends RuntimeException {
    private final int code = 35002;
    public CartItemNotFoundException(Long skuId) {
        super(String.format("购物车商品不存在，SKU ID: %d", skuId));
    }
    public int getCode() { return code; }
}