package com.commerce.platform.returns.application.handler;

import com.commerce.platform.returns.application.command.ApproveReturnCommand;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.event.ReturnApprovedEvent;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import com.commerce.platform.returns.domain.valueobject.ReturnReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ApproveReturnHandler 测试")
@ExtendWith(MockitoExtension.class)
class ApproveReturnHandlerTest {
    @Mock private ReturnRepository returnRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Captor private ArgumentCaptor<ReturnApprovedEvent> eventCaptor;
    private ApproveReturnHandler handler;

    @BeforeEach void setUp() { handler = new ApproveReturnHandler(returnRepository, eventPublisher); }

    @Test @DisplayName("审核退货应保存并发布 ApproveEvent")
    void shouldApproveReturnSuccessfully() {
        ReturnRequest r = ReturnRequest.create(1L, 100L, ReturnReason.DAMAGED);
        r.setId(10L);
        when(returnRepository.findById(10L)).thenReturn(Optional.of(r));
        when(returnRepository.save(any(ReturnRequest.class))).thenReturn(r);

        handler.handle(new ApproveReturnCommand(10L));

        verify(returnRepository).save(any(ReturnRequest.class));
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(10L, eventCaptor.getValue().getReturnId());
        assertEquals(1L, eventCaptor.getValue().getOrderId());
    }
}