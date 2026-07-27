package com.commerce.platform.cart.domain.enums;

/**
 * 结算交易状态
 * <p>
 * INIT ──start()──→ PROCESSING ──success()──→ SUCCESS
 *                                        ──fail()──→ FAILED
 * </p>
 */
public enum CheckoutStatus {
    INIT,
    PROCESSING,
    SUCCESS,
    FAILED
}