package com.commerce.platform.fulfillment.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ShipmentInfo 不可变值对象测试
 */
@DisplayName("ShipmentInfo 不可变测试")
class ShipmentInfoTest {

    @Test
    @DisplayName("ShipmentInfo 应保持不可变")
    void shouldBeImmutable() {
        LocalDateTime estimatedArrival = LocalDateTime.now().plusDays(3);
        ShipmentInfo info = new ShipmentInfo(
                "SF Express", "SF", "SF123456",
                "Beijing, China", estimatedArrival);

        // 验证所有字段通过 getter 获取
        assertEquals("SF Express", info.getCarrier());
        assertEquals("SF", info.getCarrierCode());
        assertEquals("SF123456", info.getTrackingNumber());
        assertEquals("Beijing, China", info.getShippingAddress());
        assertEquals(estimatedArrival, info.getEstimatedArrival());

        // 验证 trackingId
        assertEquals("SF:SF123456", info.getTrackingId());

        // 验证类为 final（不可继承）
        assertThrows(NoSuchMethodException.class, () -> {
            ShipmentInfo.class.getMethod("setCarrier", String.class);
        });
    }

    @Test
    @DisplayName("不同 ShipmentInfo 应相互独立")
    void shouldBeIndependentInstances() {
        ShipmentInfo info1 = new ShipmentInfo(
                "SF", "SF", "123", "Addr1", LocalDateTime.now().plusDays(1));
        ShipmentInfo info2 = new ShipmentInfo(
                "YTO", "YTO", "456", "Addr2", LocalDateTime.now().plusDays(2));

        assertNotEquals(info1.getTrackingNumber(), info2.getTrackingNumber());
        assertNotEquals(info1.getCarrier(), info2.getCarrier());
    }
}