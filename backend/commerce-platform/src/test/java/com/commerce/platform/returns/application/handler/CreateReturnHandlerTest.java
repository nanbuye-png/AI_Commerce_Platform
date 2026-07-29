package com.commerce.platform.returns.application.handler;

import com.commerce.platform.returns.application.command.CreateReturnCommand;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.event.ReturnCreatedEvent;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import com.commerce.platform.returns.domain.service.ReturnDomainService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CreateReturnHandler 测试")
@ExtendWith(MockitoExtension.class)
class CreateReturnHandlerTest {
    @Mock private ReturnRepository returnRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Captor private ArgumentCaptor<ReturnCreatedEvent> eventCaptor;
    private CreateReturnHandler handler;

    @BeforeEach void setUp() {
        handler = new CreateReturnHandler(new ReturnDomainService(), returnRepository, eventPublisher);
    }

    @Test @DisplayName("创建退货应保存并发布事件")
    void shouldCreateReturnSuccessfully() {
        ReturnRequest saved = ReturnRequest.create(1L, 100L, ReturnReason.DAMAGED);
        saved.setId(10L);
        when(returnRepository.save(any(ReturnRequest.class))).thenReturn(saved);
        handler.handle(new CreateReturnCommand(1L, 100L, ReturnReason.DAMAGED));
        verify(returnRepository).save(any(ReturnRequest.class));
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(10L, eventCaptor.getValue().getReturnId());
        assertEquals(1L, eventCaptor.getValue().getOrderId());
    }
}