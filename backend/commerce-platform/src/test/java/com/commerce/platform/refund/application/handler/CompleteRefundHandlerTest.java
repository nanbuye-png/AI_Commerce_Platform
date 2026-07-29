package com.commerce.platform.refund.application.handler;

import com.commerce.platform.refund.application.command.CompleteRefundCommand;
import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.event.RefundCompletedEvent;
import com.commerce.platform.refund.domain.repository.RefundRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CompleteRefundHandler 测试
 */
@DisplayName("CompleteRefundHandler 测试")
@ExtendWith(MockitoExtension.class)
class CompleteRefundHandlerTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<RefundCompletedEvent> eventCaptor;

    private CompleteRefundHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CompleteRefundHandler(refundRepository, eventPublisher);
    }

    @Test
    @DisplayName("完成退款应保存并发布事件")
    void shouldCompleteRefundSuccessfully() {
        // Arrange
        Long refundId = 10L;
        Refund refund = Refund.create(1L, 100L, new BigDecimal("99.99"), RefundReason.QUALITY_ISSUE);
        refund.setId(refundId);
        refund.approve();
        refund.process();

        when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));
        when(refundRepository.save(any(Refund.class))).thenReturn(refund);

        CompleteRefundCommand command = new CompleteRefundCommand(refundId);

        // Act
        handler.handle(command);

        // Assert
        verify(refundRepository).save(any(Refund.class));
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        RefundCompletedEvent event = eventCaptor.getValue();
        assertEquals(refundId, event.getRefundId());
        assertEquals(1L, event.getOrderId());
        assertNotNull(event.getCompletedAt());
    }
}