package com.commerce.platform.shipping.domain.entity;

import com.commerce.platform.shipping.domain.aggregate.Shipment;
import com.commerce.platform.shipping.domain.exception.InvalidShipmentStatusException;
import com.commerce.platform.shipping.domain.valueobject.ShipmentStatus;
import com.commerce.platform.shipping.domain.valueobject.DeliveryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shipment 状态流转测试")
class ShipmentStateTest {

    private Shipment shipment;

    @BeforeEach
    void setUp() {
        shipment = Shipment.create(100L, 50L, "SF");
    }

    @Test
    @DisplayName("创建配送单应初始化为 CREATED")
    void shouldBeCreated() {
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertEquals(DeliveryStatus.WAITING, shipment.getDeliveryStatus());
        assertEquals(100L, shipment.getFulfillmentId());
        assertEquals("SF", shipment.getCarrier());
    }

    @Test
    @DisplayName("完整状态流转应全部合法")
    void shouldCompleteFullFlow() {
        shipment.markReadyToShip();
        assertEquals(ShipmentStatus.READY_TO_SHIP, shipment.getStatus());

        shipment.ship("SF123456");
        assertEquals(ShipmentStatus.SHIPPED, shipment.getStatus());
        assertEquals("SF123456", shipment.getTrackingNumber());
        assertNotNull(shipment.getShippedAt());

        shipment.markInTransit();
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());

        shipment.markDelivered();
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        assertEquals(DeliveryStatus.DELIVERED, shipment.getDeliveryStatus());
        assertNotNull(shipment.getDeliveredAt());
    }

    @Test
    @DisplayName("从 CREATED 可以取消")
    void shouldCancelFromCreated() {
        shipment.cancel();
        assertEquals(ShipmentStatus.CANCELLED, shipment.getStatus());
    }

    @Test
    @DisplayName("从 CREATED 可以标记失败")
    void shouldFailFromCreated() {
        shipment.fail();
        assertEquals(ShipmentStatus.FAILED, shipment.getStatus());
        assertEquals(DeliveryStatus.FAILED, shipment.getDeliveryStatus());
    }

    @Test
    @DisplayName("DELIVERED 不可再流转")
    void shouldNotTransitionFromDelivered() {
        shipment.markReadyToShip();
        shipment.ship("TN");
        shipment.markInTransit();
        shipment.markDelivered();

        assertThrows(InvalidShipmentStatusException.class, () -> shipment.markInTransit());
        assertThrows(InvalidShipmentStatusException.class, () -> shipment.cancel());
    }

    @Test
    @DisplayName("CREATED → SHIPPED 非法跳转")
    void shouldNotAllowCreatedToShipped() {
        assertThrows(InvalidShipmentStatusException.class,
                () -> shipment.ship("TN"));
    }

    @Test
    @DisplayName("DELIVERED 状态不可取消或标记失败")
    void shouldNotAllowCancelOrFailAfterDelivered() {
        shipment.markReadyToShip();
        shipment.ship("TN");
        shipment.markInTransit();
        shipment.markDelivered();

        assertThrows(InvalidShipmentStatusException.class, () -> shipment.cancel());
        assertThrows(InvalidShipmentStatusException.class, () -> shipment.fail());
    }
}