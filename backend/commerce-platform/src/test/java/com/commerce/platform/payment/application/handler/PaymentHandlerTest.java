package com.commerce.platform.payment.application.handler;

import com.commerce.platform.payment.application.command.*;
import com.commerce.platform.payment.domain.aggregate.Payment;
import com.commerce.platform.payment.domain.event.*;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import com.commerce.platform.payment.domain.service.PaymentDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Handler 测试")
class PaymentHandlerTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private PaymentDomainService paymentDomainService;
    private CreatePaymentHandler createHandler;
    private StartPaymentHandler startHandler;
    private CompletePaymentHandler completeHandler;
    private FailPaymentHandler failHandler;

    @BeforeEach
    void setUp() {
        paymentDomainService = new PaymentDomainService();
        createHandler = new CreatePaymentHandler(paymentDomainService, paymentRepository, eventPublisher);
        startHandler = new StartPaymentHandler(paymentRepository, eventPublisher);
        completeHandler = new CompletePaymentHandler(paymentRepository, eventPublisher);
        failHandler = new FailPaymentHandler(paymentRepository, eventPublisher);
    }

    @Test @DisplayName("创建支付应保存并发布事件")
    void shouldSaveAndPublishOnCreate() {
        CreatePaymentCommand cmd = new CreatePaymentCommand(1L, 100L, new BigDecimal("99.99"), "PAY001");
        Payment saved = Payment.create(1L, 100L, new BigDecimal("99.99"), "PAY001");
        saved.setId(1L);
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        Payment result = createHandler.handle(cmd);
        assertNotNull(result);
        verify(paymentRepository, times(1)).save(any(Payment.class));

        ArgumentCaptor<PaymentCreatedEvent> captor = ArgumentCaptor.forClass(PaymentCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals(1L, captor.getValue().getOrderId());
    }

    @Test @DisplayName("支付成功应发布事件")
    void shouldPublishSuccessEvent() {
        Payment payment = Payment.create(1L, 100L, new BigDecimal("99.99"), "PAY002");
        payment.setId(1L);
        payment.startProcessing();
        Payment paid = Payment.create(1L, 100L, new BigDecimal("99.99"), "PAY002");
        paid.setId(1L);
        paid.startProcessing();
        paid.markPaid("TXN001");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(paid);

        CompletePaymentCommand cmd = new CompletePaymentCommand(1L, "TXN001");
        Payment result = completeHandler.handle(cmd);
        assertEquals("TXN001", result.getTransactionNo());

        ArgumentCaptor<PaymentSuccessEvent> captor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals("TXN001", captor.getValue().getTransactionNo());
    }

    @Test @DisplayName("支付失败应发布事件")
    void shouldPublishFailedEvent() {
        Payment payment = Payment.create(1L, 100L, new BigDecimal("99.99"), "PAY003");
        payment.setId(1L);
        payment.startProcessing();
        Payment failed = Payment.create(1L, 100L, new BigDecimal("99.99"), "PAY003");
        failed.setId(1L);
        failed.startProcessing();
        failed.fail();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(failed);

        FailPaymentCommand cmd = new FailPaymentCommand(1L, "余额不足");
        failHandler.handle(cmd);

        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals("余额不足", captor.getValue().getReason());
    }

    @Test @DisplayName("支付不存在应抛异常")
    void shouldThrowExceptionWhenNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> completeHandler.handle(new CompletePaymentCommand(999L, "TXN")));
        assertThrows(IllegalArgumentException.class,
                () -> failHandler.handle(new FailPaymentCommand(999L, "失败")));
    }
}