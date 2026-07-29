package com.commerce.platform.payment.application.handler;

import com.commerce.platform.payment.application.command.FailPaymentCommand;
import com.commerce.platform.payment.domain.aggregate.Payment;
import com.commerce.platform.payment.domain.event.PaymentFailedEvent;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailPaymentHandler {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public Payment handle(FailPaymentCommand command) {
        log.info("支付失败: paymentId={}, reason={}", command.getPaymentId(), command.getReason());

        Payment payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("支付不存在: paymentId=" + command.getPaymentId()));

        payment.fail();
        Payment savedPayment = paymentRepository.save(payment);

        PaymentFailedEvent event = new PaymentFailedEvent(
                savedPayment.getId(), savedPayment.getOrderId(), command.getReason());
        eventPublisher.publishEvent(event);

        log.info("支付失败已记录: paymentId={}", savedPayment.getId());
        return savedPayment;
    }
}