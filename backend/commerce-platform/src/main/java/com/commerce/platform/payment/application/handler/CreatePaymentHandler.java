package com.commerce.platform.payment.application.handler;

import com.commerce.platform.payment.application.command.CreatePaymentCommand;
import com.commerce.platform.payment.domain.aggregate.Payment;
import com.commerce.platform.payment.domain.event.PaymentCreatedEvent;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import com.commerce.platform.payment.domain.service.PaymentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePaymentHandler {

    private final PaymentDomainService paymentDomainService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public Payment handle(CreatePaymentCommand command) {
        log.info("开始创建支付: orderId={}, userId={}, amount={}",
                command.getOrderId(), command.getUserId(), command.getAmount());

        Payment payment = paymentDomainService.createPayment(
                command.getOrderId(), command.getUserId(),
                command.getAmount(), command.getPaymentNo());

        Payment savedPayment = paymentRepository.save(payment);

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                savedPayment.getId(), savedPayment.getOrderId(), savedPayment.getAmount());
        eventPublisher.publishEvent(event);

        log.info("支付创建成功: paymentId={}, orderId={}", savedPayment.getId(), savedPayment.getOrderId());
        return savedPayment;
    }
}