package com.commerce.platform.cart.exception;

public class CartNotFoundException extends RuntimeException {
    private final int code = 35001;
    public CartNotFoundException(Long userId) {
        super(String.format("购物车不存在，用户ID: %d", userId));
    }
    public int getCode() { return code; }
}