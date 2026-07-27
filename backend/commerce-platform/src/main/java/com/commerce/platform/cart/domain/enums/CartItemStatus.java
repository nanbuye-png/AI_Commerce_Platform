package com.commerce.platform.cart.domain.enums;

/**
 * 购物车商品状态
 * <p>
 * ACTIVE ──remove()──→ REMOVED
 * ACTIVE ──checkout()──→ CHECKED_OUT
 * </p>
 */
public enum CartItemStatus {
    ACTIVE,
    REMOVED,
    CHECKED_OUT
}