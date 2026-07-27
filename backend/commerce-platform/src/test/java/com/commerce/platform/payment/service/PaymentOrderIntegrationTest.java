package com.commerce.platform.payment.service;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.payment.event.PaymentSuccessEvent;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Payment ↔ Order 集成测试
 * <p>
 * 测试 @TransactionalEventListener(AFTER_COMMIT) 在事务提交后的行为。
 * 不使用 @Rollback，手动清理数据。
 * </p>
 */
@SpringBootTest
class PaymentOrderIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("正常流程：支付成功事件 → 订单状态变更为 PAID")
    void shouldUpdateOrderToPaidWhenPaymentSucceeds() {
        Order order = orderRepository.save(Order.builder()
                .orderNo("TEST_ORDER_" + System.currentTimeMillis())
                .buyerId(1L).merchantId(1L).storeId(1L)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.valueOf(100))
                .productAmount(BigDecimal.valueOf(100))
                .payAmount(BigDecimal.valueOf(100))
                .build());

        eventPublisher.publishEvent(new PaymentSuccessEvent(
                "PAY_TEST_001", order.getOrderNo(), "MOCK_TXN_001", BigDecimal.valueOf(100)));

        entityManager.clear();
        Optional<Order> found = orderRepository.findByOrderNo(order.getOrderNo());
        assertTrue(found.isPresent(), "订单应存在");
        assertEquals(OrderStatus.PAID, found.get().getOrderStatus(),
                "支付成功后订单状态应为 PAID");
    }

    @Test
    @DisplayName("重复事件：订单已 PAID，再次收到支付成功事件应忽略且不报错")
    void shouldIgnoreDuplicatePaymentSuccessEvent() {
        Order order = orderRepository.save(Order.builder()
                .orderNo("TEST_ORDER_DUP_" + System.currentTimeMillis())
                .buyerId(1L).merchantId(1L).storeId(1L)
                .orderStatus(OrderStatus.PAID)
                .totalAmount(BigDecimal.valueOf(100))
                .productAmount(BigDecimal.valueOf(100))
                .payAmount(BigDecimal.valueOf(100))
                .build());

        assertDoesNotThrow(() -> eventPublisher.publishEvent(new PaymentSuccessEvent(
                "PAY_TEST_002", order.getOrderNo(), "MOCK_TXN_002", BigDecimal.valueOf(100))),
                "重复支付事件不应抛出异常");

        entityManager.clear();
        Optional<Order> found = orderRepository.findByOrderNo(order.getOrderNo());
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.PAID, found.get().getOrderStatus(),
                "重复事件不应改变订单状态");
    }

    @Test
    @DisplayName("完整流程：支付成功 → Order PAID → paymentTime 已记录")
    void shouldPublishOrderPaidEventAfterPaymentSuccess() {
        Order order = orderRepository.save(Order.builder()
                .orderNo("TEST_ORDER_FULL_" + System.currentTimeMillis())
                .buyerId(1L).merchantId(1L).storeId(1L)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.valueOf(200))
                .productAmount(BigDecimal.valueOf(200))
                .payAmount(BigDecimal.valueOf(200))
                .build());

        eventPublisher.publishEvent(new PaymentSuccessEvent(
                "PAY_FULL_" + System.currentTimeMillis(), order.getOrderNo(),
                "MOCK_TXN_FULL", BigDecimal.valueOf(200)));

        entityManager.clear();
        Optional<Order> found = orderRepository.findById(order.getId());
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.PAID, found.get().getOrderStatus(),
                "订单应已支付");
        assertNotNull(found.get().getPaymentTime(),
                "支付时间应已记录");
    }
}