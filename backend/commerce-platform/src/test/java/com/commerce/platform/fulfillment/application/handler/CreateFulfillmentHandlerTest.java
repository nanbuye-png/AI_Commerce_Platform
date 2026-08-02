package com.commerce.platform.fulfillment.application.handler;

import com.commerce.platform.fulfillment.application.command.CreateFulfillmentCommand;
import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.event.FulfillmentCreatedEvent;
import com.commerce.platform.fulfillment.domain.repository.FulfillmentRepository;
import com.commerce.platform.fulfillment.domain.service.FulfillmentDomainService;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CreateFulfillmentHandler 测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateFulfillmentHandler 测试")
class CreateFulfillmentHandlerTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private FulfillmentDomainService domainService;
    private CreateFulfillmentHandler handler;

    @BeforeEach
    void setUp() {
        domainService = new FulfillmentDomainService(fulfillmentRepository);
        handler = new CreateFulfillmentHandler(domainService, fulfillmentRepository, eventPublisher);
    }

    @Test
    @DisplayName("处理创建履约单命令应保存并发布事件")
    void shouldSaveAndPublishEvent() {
        // Arrange
        when(fulfillmentRepository.existsByOrderId(1L)).thenReturn(false);
        when(fulfillmentRepository.save(any(Fulfillment.class)))
                .thenAnswer(invocation -> {
                    Fulfillment f = invocation.getArgument(0);
                    f.setId(100L);
                    return f;
                });

        CreateFulfillmentCommand command = new CreateFulfillmentCommand(1L, 200L);

        // Act
        Fulfillment result = handler.handle(command);

        // Assert - 保存
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getOrderId());
        assertEquals(200L, result.getMerchantId());
        assertEquals(FulfillmentStatus.PENDING, result.getStatus());

        verify(fulfillmentRepository).save(any(Fulfillment.class));

        // Assert - 事件发布
        ArgumentCaptor<FulfillmentCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(FulfillmentCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        FulfillmentCreatedEvent event = eventCaptor.getValue();
        assertEquals(100L, event.getFulfillmentId());
        assertEquals(1L, event.getOrderId());
        assertNotNull(event.getOccurredOn());
    }

    @Test
    @DisplayName("订单已有履约单时应抛出异常")
    void shouldThrowExceptionWhenAlreadyExists() {
        when(fulfillmentRepository.existsByOrderId(1L)).thenReturn(true);

        CreateFulfillmentCommand command = new CreateFulfillmentCommand(1L, 200L);

        assertThrows(IllegalStateException.class, () -> handler.handle(command));

        verify(fulfillmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}