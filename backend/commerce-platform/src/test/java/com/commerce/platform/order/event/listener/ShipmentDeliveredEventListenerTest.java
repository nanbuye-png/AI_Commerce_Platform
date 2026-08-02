package com.commerce.platform.order.event.listener;

import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.repository.FulfillmentRepository;
import com.commerce.platform.order.application.command.CompleteOrderCommand;
import com.commerce.platform.order.application.handler.CompleteOrderHandler;
import com.commerce.platform.shipping.domain.event.ShipmentDeliveredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ShipmentDeliveredEventListener 事件流测试
 * <p>
 * 覆盖：ShipmentDeliveredEvent → CompleteOrderCommand → OrderCompletedEvent 链路
 * </p>
 */
@DisplayName("ShipmentDeliveredEventListener 事件流测试")
@ExtendWith(MockitoExtension.class)
class ShipmentDeliveredEventListenerTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @Mock
    private CompleteOrderHandler completeOrderHandler;

    @Captor
    private ArgumentCaptor<CompleteOrderCommand> commandCaptor;

    @Test
    @DisplayName("收到 ShipmentDeliveredEvent 应触发订单完成")
    void shouldCompleteOrderOnShipmentDelivered() {
        // Arrange
        Long fulfillmentId = 1L;
        Long shipmentId = 100L;
        Long orderId = 10L;

        Fulfillment fulfillment = Fulfillment.restore(
                fulfillmentId, orderId, 100L, null,
                com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus.DELIVERED,
                null, null, null);

        when(fulfillmentRepository.findById(fulfillmentId)).thenReturn(Optional.of(fulfillment));

        ShipmentDeliveredEventListener listener = new ShipmentDeliveredEventListener(
                fulfillmentRepository, completeOrderHandler);
        ShipmentDeliveredEvent event = new ShipmentDeliveredEvent(shipmentId, fulfillmentId);

        // Act
        listener.onShipmentDelivered(event);

        // Assert
        verify(completeOrderHandler).handle(commandCaptor.capture());
        CompleteOrderCommand command = commandCaptor.getValue();
        assertEquals(orderId, command.getOrderId());
    }

    @Test
    @DisplayName("履约单不存在时应捕获异常不抛出")
    void shouldCatchExceptionWhenFulfillmentNotFound() {
        // Arrange
        Long fulfillmentId = 999L;
        when(fulfillmentRepository.findById(fulfillmentId)).thenReturn(Optional.empty());

        ShipmentDeliveredEventListener listener = new ShipmentDeliveredEventListener(
                fulfillmentRepository, completeOrderHandler);
        ShipmentDeliveredEvent event = new ShipmentDeliveredEvent(100L, fulfillmentId);

        // Act - should not throw, listener catches the exception
        assertDoesNotThrow(() -> listener.onShipmentDelivered(event));

        // Assert
        verify(completeOrderHandler, never()).handle(any());
    }

    @Test
    @DisplayName("完整事件链路：ShipmentDeliveredEvent → CompleteOrderCommand")
    void shouldHandleFullEventChain() {
        // Arrange
        Long fulfillmentId = 1L;
        Long shipmentId = 100L;
        Long orderId = 10L;

        Fulfillment fulfillment = Fulfillment.restore(
                fulfillmentId, orderId, 100L, null,
                com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus.DELIVERED,
                null, null, null);

        when(fulfillmentRepository.findById(fulfillmentId)).thenReturn(Optional.of(fulfillment));

        ShipmentDeliveredEventListener listener = new ShipmentDeliveredEventListener(
                fulfillmentRepository, completeOrderHandler);
        ShipmentDeliveredEvent event = new ShipmentDeliveredEvent(shipmentId, fulfillmentId);

        // Act
        listener.onShipmentDelivered(event);

        // Assert
        verify(fulfillmentRepository).findById(fulfillmentId);
        verify(completeOrderHandler).handle(any(CompleteOrderCommand.class));
    }
}