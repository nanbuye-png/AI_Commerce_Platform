package com.commerce.platform.order.application.handler;

import com.commerce.platform.order.application.command.CompleteOrderCommand;
import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.event.OrderCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CompleteOrderHandler 测试
 * <p>
 * 覆盖：Command 成功执行，Repository 保存，事件发布
 * </p>
 */
@DisplayName("CompleteOrderHandler 测试")
@ExtendWith(MockitoExtension.class)
class CompleteOrderHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<OrderCompletedEvent> eventCaptor;

    private CompleteOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CompleteOrderHandler(orderRepository, eventPublisher);
    }

    private Order createShippedOrder(Long id, String orderNo) {
        Order order = Order.builder()
                .orderNo(orderNo)
                .buyerId(1L)
                .merchantId(100L)
                .storeId(10L)
                .totalAmount(new BigDecimal("100.00"))
                .productAmount(new BigDecimal("90.00"))
                .freightAmount(new BigDecimal("10.00"))
                .payAmount(new BigDecimal("100.00"))
                .build();
        order.setId(id);
        // Simulate the state transitions to reach SHIPPED
        order.pay();
        order.ship();
        return order;
    }

    @Test
    @DisplayName("处理完成命令应成功完成订单")
    void shouldCompleteOrderSuccessfully() {
        // Arrange
        Long orderId = 1L;
        String orderNo = "ORD202607280001";
        Order order = createShippedOrder(orderId, orderNo);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        CompleteOrderCommand command = new CompleteOrderCommand(orderId);

        // Act
        handler.handle(command);

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.COMPLETED, savedOrder.getOrderStatus());
        assertNotNull(savedOrder.getCompletedTime());

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderCompletedEvent event = eventCaptor.getValue();
        assertEquals(orderNo, event.getOrderNo());
        assertEquals(1L, event.getBuyerId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    @DisplayName("订单不存在时应抛出异常")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        CompleteOrderCommand command = new CompleteOrderCommand(orderId);

        // Act & Assert
        assertThrows(com.commerce.platform.order.exception.OrderNotFoundException.class,
                () -> handler.handle(command));

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("已完成的订单应幂等忽略")
    void shouldSkipWhenOrderAlreadyCompleted() {
        // Arrange
        Long orderId = 1L;
        String orderNo = "ORD202607280001";
        Order order = createShippedOrder(orderId, orderNo);
        order.complete(); // Already COMPLETED
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        CompleteOrderCommand command = new CompleteOrderCommand(orderId);

        // Act
        handler.handle(command);

        // Assert
        verify(orderRepository).findById(orderId);
        // save() should not be called since it's already COMPLETED
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}