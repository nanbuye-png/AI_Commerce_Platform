package com.commerce.platform.refund.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentRefundGateway 测试
 * <p>
 * 验证 Interface 存在且 Adapter 可替换。
 * </p>
 */
@DisplayName("PaymentRefundGateway 测试")
class PaymentRefundGatewayTest {

    @Test
    @DisplayName("接口应定义 refund 方法")
    void shouldHaveRefundMethod() throws NoSuchMethodException {
        // Verify the interface has the refund method
        java.lang.reflect.Method method = PaymentRefundGateway.class.getMethod(
                "refund", String.class, BigDecimal.class, String.class);
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    @DisplayName("Adapter 可替换性验证 - 匿名实现应可创建")
    void shouldBeReplaceableWithAdapter() {
        // Mock adapter to verify substitutability
        PaymentRefundGateway gateway = (orderNo, amount, refundNo) -> {
            assertNotNull(orderNo);
            assertNotNull(amount);
            assertNotNull(refundNo);
            return true;
        };

        boolean result = gateway.refund("ORD001", new BigDecimal("99.99"), "REF001");
        assertTrue(result);
    }
}