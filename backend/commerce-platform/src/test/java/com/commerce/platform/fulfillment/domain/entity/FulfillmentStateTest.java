package com.commerce.platform.fulfillment.domain.entity;

import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.exception.InvalidFulfillmentStatusException;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import com.commerce.platform.fulfillment.domain.valueobject.ShipmentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 履约单状态流转测试
 */
@DisplayName("Fulfillment 状态流转测试")
class FulfillmentStateTest {

    private Fulfillment fulfillment;

    @BeforeEach
    void setUp() {
        fulfillment = Fulfillment.create(1L, 100L);
    }

    @Test
    @DisplayName("创建履约单应初始化为 PENDING 状态")
    void shouldBePendingWhenCreated() {
        assertEquals(FulfillmentStatus.PENDING, fulfillment.getStatus());
        assertEquals(1L, fulfillment.getOrderId());
        assertEquals(100L, fulfillment.getMerchantId());
    }

    @Test
    @DisplayName("完整状态流转应全部合法")
    void shouldTransitionThroughAllValidStates() {
        fulfillment.startProcessing();
        assertEquals(FulfillmentStatus.PROCESSING, fulfillment.getStatus());

        fulfillment.startPicking();
        assertEquals(FulfillmentStatus.PICKING, fulfillment.getStatus());

        fulfillment.startPacking();
        assertEquals(FulfillmentStatus.PACKING, fulfillment.getStatus());

        fulfillment.markWaitingShipment();
        assertEquals(FulfillmentStatus.WAITING_SHIPMENT, fulfillment.getStatus());

        ShipmentInfo shipmentInfo = new ShipmentInfo(
                "SF Express", "SF", "SF123456789",
                "Beijing, China", LocalDateTime.now().plusDays(3));
        fulfillment.ship(shipmentInfo);
        assertEquals(FulfillmentStatus.SHIPPED, fulfillment.getStatus());

        fulfillment.deliver();
        assertEquals(FulfillmentStatus.DELIVERED, fulfillment.getStatus());

        fulfillment.complete();
        assertEquals(FulfillmentStatus.COMPLETED, fulfillment.getStatus());
    }

    @Test
    @DisplayName("从 PENDING 可以取消")
    void shouldAllowCancelFromPending() {
        fulfillment.cancel();
        assertEquals(FulfillmentStatus.CANCELLED, fulfillment.getStatus());
    }

    @Test
    @DisplayName("从 PROCESSING 可以取消")
    void shouldAllowCancelFromProcessing() {
        fulfillment.startProcessing();
        fulfillment.cancel();
        assertEquals(FulfillmentStatus.CANCELLED, fulfillment.getStatus());
    }

    @Test
    @DisplayName("从 WAITING_SHIPMENT 可以取消")
    void shouldAllowCancelFromWaitingShipment() {
        fulfillment.startProcessing();
        fulfillment.startPicking();
        fulfillment.startPacking();
        fulfillment.markWaitingShipment();
        fulfillment.cancel();
        assertEquals(FulfillmentStatus.CANCELLED, fulfillment.getStatus());
    }

    @Test
    @DisplayName("从任意非终态可以标记失败")
    void shouldAllowFailFromNonFinalStates() {
        fulfillment.fail();
        assertEquals(FulfillmentStatus.FAILED, fulfillment.getStatus());
    }

    @Test
    @DisplayName("COMPLETED 状态不可继续流转")
    void shouldNotTransitionFromCompleted() {
        fulfillment.startProcessing();
        fulfillment.startPicking();
        fulfillment.startPacking();
        fulfillment.markWaitingShipment();
        fulfillment.ship(new ShipmentInfo("SF", "SF", "123", "Addr", LocalDateTime.now().plusDays(1)));
        fulfillment.deliver();
        fulfillment.complete();

        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.startProcessing());
        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.cancel());
        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.fail());
    }

    @Test
    @DisplayName("CANCELLED 状态不可继续流转")
    void shouldNotTransitionFromCancelled() {
        fulfillment.cancel();

        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.startProcessing());
        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.fail());
    }

    @Test
    @DisplayName("FAILED 状态不可继续流转")
    void shouldNotTransitionFromFailed() {
        fulfillment.fail();

        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.startProcessing());
        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.cancel());
    }

    @Test
    @DisplayName("非法跳转：PENDING → SHIPPED 应抛异常")
    void shouldThrowExceptionForInvalidTransition() {
        assertThrows(InvalidFulfillmentStatusException.class, () -> {
            fulfillment.ship(new ShipmentInfo("SF", "SF", "123", "Addr", null));
        });
    }

    @Test
    @DisplayName("非法跳转：PENDING → COMPLETED 应抛异常")
    void shouldThrowExceptionForPendingToCompleted() {
        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.complete());
    }

    @Test
    @DisplayName("非法跳转：SHIPPED → PICKING 应抛异常（回退）")
    void shouldThrowExceptionForShippedBackToPicking() {
        fulfillment.startProcessing();
        fulfillment.startPicking();
        fulfillment.startPacking();
        fulfillment.markWaitingShipment();
        fulfillment.ship(new ShipmentInfo("SF", "SF", "123", "Addr", null));

        assertThrows(InvalidFulfillmentStatusException.class, () -> fulfillment.startPicking());
    }
}