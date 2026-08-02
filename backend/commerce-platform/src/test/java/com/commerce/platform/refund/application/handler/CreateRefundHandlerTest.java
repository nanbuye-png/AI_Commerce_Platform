package com.commerce.platform.refund.application.handler;

import com.commerce.platform.refund.application.command.CreateRefundCommand;
import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.event.RefundCreatedEvent;
import com.commerce.platform.refund.domain.repository.RefundRepository;
import com.commerce.platform.refund.domain.service.RefundDomainService;
import com.commerce.platform.refund.domain.valueobject.RefundReason;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CreateRefundHandler 测试
 */
@DisplayName("CreateRefundHandler 测试")
@ExtendWith(MockitoExtension.class)
class CreateRefundHandlerTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<RefundCreatedEvent> eventCaptor;

    private RefundDomainService refundDomainService;
    private CreateRefundHandler handler;

    @BeforeEach
    void setUp() {
        refundDomainService = new RefundDomainService();
        handler = new CreateRefundHandler(refundDomainService, refundRepository, eventPublisher);
    }

    @Test
    @DisplayName("创建退款应保存并发布事件")
    void shouldCreateRefundSuccessfully() {
        // Arrange
        CreateRefundCommand command = new CreateRefundCommand(
                1L, 100L, new BigDecimal("99.99"), RefundReason.QUALITY_ISSUE);

        Refund saved = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.QUALITY_ISSUE);
        saved.setId(10L);

        when(refundRepository.save(any(Refund.class))).thenReturn(saved);

        // Act
        handler.handle(command);

        // Assert
        verify(refundRepository).save(any(Refund.class));
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        RefundCreatedEvent event = eventCaptor.getValue();
        assertEquals(10L, event.getRefundId());
        assertEquals(1L, event.getOrderId());
        assertEquals(100L, event.getUserId());
        assertNotNull(event.getOccurredAt());
    }
}