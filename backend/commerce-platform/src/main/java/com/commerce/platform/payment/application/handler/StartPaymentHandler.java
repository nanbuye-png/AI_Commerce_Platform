package com.commerce.platform.payment.application.handler;

import com.commerce.platform.payment.application.command.StartPaymentCommand;
import com.commerce.platform.payment.domain.aggregate.Payment;
import com.commerce.platform.payment.domain.event.PaymentStartedEvent;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartPaymentHandler {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public Payment handle(StartPaymentCommand command) {
        log.info("开始处理支付: paymentId={}", command.getPaymentId());

        Payment payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("支付不存在: paymentId=" + command.getPaymentId()));

        payment.startProcessing();
        Payment savedPayment = paymentRepository.save(payment);

        PaymentStartedEvent event = new PaymentStartedEvent(savedPayment.getId(), savedPayment.getOrderId());
        eventPublisher.publishEvent(event);

        log.info("支付处理中: paymentId={}", savedPayment.getId());
        return savedPayment;
    }
}