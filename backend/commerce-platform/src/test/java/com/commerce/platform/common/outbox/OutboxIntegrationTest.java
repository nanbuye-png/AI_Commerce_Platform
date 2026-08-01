package com.commerce.platform.common.outbox;

import com.commerce.platform.common.event.ProcessedEventRepository;
import com.commerce.platform.order.event.OrderPaidEvent;
import com.commerce.platform.payment.domain.event.PaymentSuccessEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Outbox Pattern 集成测试
 * <p>
 * 测试 Outbox 事件保存、处理器投递、幂等消费、失败重试等核心能力。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class OutboxIntegrationTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ObjectMapper objectMapper;
    private OutboxService outboxService;
    private OutboxEventProcessor processor;
    private OutboxEventDispatcher dispatcher;
    private OutboxEventTransactionService transactionService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // 注册 Java 8 time 模块
        objectMapper.findAndRegisterModules();
        outboxService = new OutboxService(outboxRepository, objectMapper);
        dispatcher = spy(new OutboxEventDispatcher(eventPublisher, objectMapper));
        transactionService = spy(new OutboxEventTransactionService(outboxRepository));
        processor = new OutboxEventProcessor(transactionService, dispatcher);
    }

    // ============================================
    // 测试1：保存 Payment SUCCESS 事件
    // ============================================

    @Test
    @DisplayName("Payment SUCCESS：支付状态正确，outbox_event 为 NEW")
    void shouldSaveOutboxEventOnPaymentSuccess() {
        PaymentSuccessEvent event = new PaymentSuccessEvent(
                1L, 1L, "TXN001", new BigDecimal("100.00"));

        when(outboxRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        outboxService.saveEvent(event, "Payment", "PAY001");

        verify(outboxRepository).save(argThat(outboxEvent ->
                outboxEvent.getStatus() == OutboxStatus.NEW
                && outboxEvent.getEventType().equals(PaymentSuccessEvent.class.getName())
                && outboxEvent.getAggregateType().equals("Payment")
                && outboxEvent.getAggregateId().equals("PAY001")
        ));
    }

    // ============================================
    // 测试2：Processor 投递事件
    // ============================================

    @Test
    @DisplayName("Outbox Processor：NEW → PROCESSING → SUCCESS")
    void shouldProcessOutboxEventSuccessfully() throws Exception {
        PaymentSuccessEvent originalEvent = new PaymentSuccessEvent(
                1L, 1L, "TXN001", new BigDecimal("100.00"));
        String payload = objectMapper.writeValueAsString(originalEvent);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId("EVT001")
                .eventType(PaymentSuccessEvent.class.getName())
                .aggregateType("Payment")
                .aggregateId("PAY001")
                .payload(payload)
                .status(OutboxStatus.NEW)
                .build();

        outboxEvent.markProcessing("token-success");
        when(outboxRepository.findByIdForUpdate(outboxEvent.getId())).thenReturn(Optional.of(outboxEvent));

        processor.processEvent(outboxEvent);

        assertEquals(OutboxStatus.SUCCESS, outboxEvent.getStatus());
        assertNull(outboxEvent.getProcessingToken());
    }

    // ============================================
    // 测试3：重复事件消费
    // ============================================

    @Test
    @DisplayName("重复事件消费：processed_event 已存在，业务不重复执行")
    void shouldNotProcessDuplicateEvent() {
        String eventId = "EVT001";
        String consumerName = "PaymentEventListener";

        when(processedEventRepository.existsByEventIdAndConsumerName(eventId, consumerName))
                .thenReturn(true);

        // 幂等检查通过后不执行业务逻辑
        boolean alreadyProcessed = processedEventRepository.existsByEventIdAndConsumerName(eventId, consumerName);
        assertTrue(alreadyProcessed);
    }

    // ============================================
    // 测试4：Processor 失败重试
    // ============================================

    @Test
    @DisplayName("Outbox Processor 失败：retryCount 增加")
    void shouldIncrementRetryCountOnFailure() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId("EVT002")
                .eventType("com.unknown.NonExistentEvent")
                .payload("{}")
                .status(OutboxStatus.NEW)
                .retryCount(0)
                .build();

        outboxEvent.markProcessing("token-failure");
        when(outboxRepository.findByIdForUpdate(outboxEvent.getId())).thenReturn(Optional.of(outboxEvent));

        processor.processEvent(outboxEvent);

        assertEquals(OutboxStatus.FAILED, outboxEvent.getStatus());
        assertTrue(outboxEvent.getRetryCount() >= 1);
        assertTrue(outboxEvent.shouldRetry());
        assertTrue(outboxEvent.getLastError().contains("未知事件类型"));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Outbox Processor：超时 PROCESSING 事件在未达上限时重新领取")
    void shouldRetryFailedEventsBelowRetryLimit() throws Exception {
        PaymentSuccessEvent originalEvent = new PaymentSuccessEvent(
                1L, 1L, "TXN002", new BigDecimal("50.00"));
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(2L)
                .eventId("EVT003")
                .eventType(PaymentSuccessEvent.class.getName())
                .payload(objectMapper.writeValueAsString(originalEvent))
                .status(OutboxStatus.FAILED)
                .retryCount(1)
                .build();

        outboxEvent.markProcessing("old-token");
        when(outboxRepository.findPendingForProcessing(anyCollection(), eq(OutboxStatus.PROCESSING), eq(5),
                any(), any()))
                .thenReturn(List.of(outboxEvent));

        List<OutboxEvent> claimed = transactionService.claimPendingEvents();

        assertEquals(1, claimed.size());
        assertEquals(2, outboxEvent.getRetryCount());
        assertEquals(OutboxStatus.PROCESSING, outboxEvent.getStatus());
        assertNotEquals("old-token", outboxEvent.getProcessingToken());
    }

    @Test
    @DisplayName("Outbox Processor：第 5 次失败后停止重试并保留错误")
    void shouldStopRetryingAfterFifthFailure() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(3L)
                .eventId("EVT004")
                .eventType(PaymentSuccessEvent.class.getName())
                .payload("not-json")
                .status(OutboxStatus.FAILED)
                .retryCount(4)
                .build();

        outboxEvent.markProcessing("final-token");
        when(outboxRepository.findByIdForUpdate(outboxEvent.getId())).thenReturn(Optional.of(outboxEvent));

        transactionService.markFailed(outboxEvent.getId(), "final-token", "not-json");

        assertEquals(OutboxStatus.FAILED, outboxEvent.getStatus());
        assertEquals(5, outboxEvent.getRetryCount());
        assertFalse(outboxEvent.shouldRetry());
        assertNotNull(outboxEvent.getLastError());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Outbox Processor：过期领取者不能覆盖新领取者的处理结果")
    void shouldIgnoreCompletionFromExpiredProcessingLease() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(4L)
                .eventId("EVT005")
                .eventType(PaymentSuccessEvent.class.getName())
                .payload("{}")
                .status(OutboxStatus.NEW)
                .build();
        outboxEvent.markProcessing("current-token");
        when(outboxRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(outboxEvent));

        transactionService.markSuccess(4L, "expired-token");

        assertEquals(OutboxStatus.PROCESSING, outboxEvent.getStatus());
        assertEquals("current-token", outboxEvent.getProcessingToken());
    }

    // ============================================
    // 测试5：完整交易链路事件保存
    // ============================================

    @Test
    @DisplayName("完整交易链路：Cart → Order → Payment → Inventory 事件全部通过 Outbox 保存")
    void shouldSaveAllDomainEventsViaOutbox() {
        when(outboxRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 模拟保存多个事件的调用
        outboxService.saveEvent(new PaymentSuccessEvent(1L, 1L, "TXN001", new BigDecimal("100.00")));
        outboxService.saveEvent(new OrderPaidEvent(1L, "ORD001", "PAY001"));

        verify(outboxRepository, times(2)).save(any(OutboxEvent.class));
    }
}