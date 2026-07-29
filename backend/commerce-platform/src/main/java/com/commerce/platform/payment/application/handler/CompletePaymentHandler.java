package com.commerce.platform.payment.application.handler;

import com.commerce.platform.payment.application.command.CompletePaymentCommand;
import com.commerce.platform.payment.domain.aggregate.Payment;
import com.commerce.platform.payment.domain.event.PaymentSuccessEvent;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletePaymentHandler {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public Payment handle(CompletePaymentCommand command) {
        log.info("完成支付: paymentId={}, transactionNo={}", command.getPaymentId(), command.getTransactionNo());

        Payment payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("支付不存在: paymentId=" + command.getPaymentId()));

        payment.markPaid(command.getTransactionNo());
        Payment savedPayment = paymentRepository.save(payment);

        PaymentSuccessEvent event = new PaymentSuccessEvent(
                savedPayment.getId(), savedPayment.getOrderId(),
                savedPayment.getTransactionNo(), savedPayment.getAmount());
        eventPublisher.publishEvent(event);

        log.info("支付成功: paymentId={}, orderId={}", savedPayment.getId(), savedPayment.getOrderId());
        return savedPayment;
    }
}