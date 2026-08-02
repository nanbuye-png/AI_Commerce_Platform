package com.commerce.platform.inventory.reservation.domain.entity;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.exception.InvalidReservationStatusException;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
/**
 * 库存预占状态流转测试
 */
@DisplayName("StockReservation 状态流转测试")
class StockReservationStateTest {

    private StockReservation reservation;

    @BeforeEach
    void setUp() {
        reservation = StockReservation.create(1L, 100L, 5);
    }

    @Test
    @DisplayName("创建预占应初始化为 RESERVED 状态")
    void shouldBeReservedWhenCreated() {
        assertEquals(ReservationStatus.RESERVED, reservation.getStatus());
        assertEquals(1L, reservation.getOrderId());
        assertEquals(100L, reservation.getProductId());
        assertEquals(5, reservation.getQuantity());
    }

    @Test
    @DisplayName("RESERVED → CONFIRMED 应合法")
    void shouldTransitionFromReservedToConfirmed() {
        reservation.confirm();
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertNotNull(reservation.getConfirmedAt());
    }

    @Test
    @DisplayName("RESERVED → RELEASED 应合法")
    void shouldTransitionFromReservedToReleased() {
        reservation.release();
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        assertNotNull(reservation.getReleasedAt());
    }

    @Test
    @DisplayName("RESERVED → FAILED 应合法")
    void shouldTransitionFromReservedToFailed() {
        reservation.fail();
        assertEquals(ReservationStatus.FAILED, reservation.getStatus());
    }

    @Test
    @DisplayName("CONFIRMED 不可再次释放")
    void shouldNotAllowReleaseFromConfirmed() {
        reservation.confirm();
        assertThrows(InvalidReservationStatusException.class, () -> reservation.release());
    }

    @Test
    @DisplayName("CONFIRMED 不可再次确认")
    void shouldNotAllowConfirmFromConfirmed() {
        reservation.confirm();
        assertThrows(InvalidReservationStatusException.class, () -> reservation.confirm());
    }

    @Test
    @DisplayName("CONFIRMED 不可标记失败")
    void shouldNotAllowFailFromConfirmed() {
        reservation.confirm();
        assertThrows(InvalidReservationStatusException.class, () -> reservation.fail());
    }

    @Test
    @DisplayName("RELEASED 不可再次释放")
    void shouldNotAllowReleaseFromReleased() {
        reservation.release();
        assertThrows(InvalidReservationStatusException.class, () -> reservation.release());
    }

    @Test
    @DisplayName("RELEASED 不可确认")
    void shouldNotAllowConfirmFromReleased() {
        reservation.release();
        assertThrows(InvalidReservationStatusException.class, () -> reservation.confirm());
    }

    @Test
    @DisplayName("RELEASED 不可标记失败")
    void shouldNotAllowFailFromReleased() {
        reservation.release();
        assertThrows(InvalidReservationStatusException.class, () -> reservation.fail());
    }

    @Test
    @DisplayName("FAILED 不可继续流转")
    void shouldNotTransitionFromFailed() {
        reservation.fail();
        assertThrows(InvalidReservationStatusException.class, () -> reservation.confirm());
        assertThrows(InvalidReservationStatusException.class, () -> reservation.release());
        assertThrows(InvalidReservationStatusException.class, () -> reservation.fail());
    }

    @Test
    @DisplayName("非法跳转：RESERVED →（CONFIRMED → RELEASED）不可行")
    void shouldThrowExceptionForInvalidTransitionChain() {
        reservation.confirm();
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertThrows(InvalidReservationStatusException.class, () -> reservation.release());
    }

    @Test
    @DisplayName("restore 方法应正确恢复所有字段")
    void shouldRestoreAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StockReservation restored = StockReservation.restore(
                1L, 2L, 200L, 10,
                ReservationStatus.CONFIRMED,
                now,
                now.plusHours(1),
                null
        );

        assertEquals(1L, restored.getId());
        assertEquals(2L, restored.getOrderId());
        assertEquals(200L, restored.getProductId());
        assertEquals(10, restored.getQuantity());
        assertEquals(ReservationStatus.CONFIRMED, restored.getStatus());
        assertNotNull(restored.getCreatedAt());
        assertNotNull(restored.getConfirmedAt());
        assertNull(restored.getReleasedAt());
    }
}