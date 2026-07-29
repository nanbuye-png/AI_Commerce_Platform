package com.commerce.platform.payment.event.listener;

import com.commerce.platform.payment.domain.entity.Payment;
import com.commerce.platform.payment.domain.enums.PaymentMethod;
import com.commerce.platform.payment.domain.enums.PaymentStatus;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import com.commerce.platform.payment.event.OrderCreatedForPaymentEvent;
import com.commerce.platform.payment.event.PaymentCreatedEvent;
import com.commerce.platform.payment.provider.PaymentNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单创建支付监听器（属于 Payment Domain）
 * <p>
 * 监听 Order Domain 的 OrderCreatedForPaymentEvent，自动创建支付记录。
 * Payment 不依赖 Order，只依赖 Order 发布的 Event 进行解耦通信。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedPaymentListener {

    private final PaymentRepository paymentRepository;
    private final PaymentNoGenerator paymentNoGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedForPaymentEvent event) {
        log.info("收到订单创建事件，开始创建支付：orderNo={}, amount={}, userId={}",
                event.getOrderNo(), event.getAmount(), event.getUserId());

        // 幂等处理：检查是否已存在支付记录
        if (paymentRepository.findByOrderNo(event.getOrderNo()).isPresent()) {
            log.warn("支付记录已存在，跳过重复创建：orderNo={}", event.getOrderNo());
            return;
        }

        try {
            String paymentNo = paymentNoGenerator.generate();

            Payment payment = Payment.builder()
                    .paymentNo(paymentNo)
                    .orderNo(event.getOrderNo())
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .paymentMethod(PaymentMethod.MOCK)
                    .paymentStatus(PaymentStatus.CREATED)
                    .build();

            paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentCreatedEvent(paymentNo, event.getOrderNo(), event.getUserId()));

            log.info("支付创建成功：paymentNo={}, orderNo={}", paymentNo, event.getOrderNo());

        } catch (Exception e) {
            log.error("支付创建失败：orderNo={}, error={}", event.getOrderNo(), e.getMessage(), e);
            // 支付创建失败异常不抛给 Order 侧，避免影响 Order 创建事务
            // 后续可通过补偿机制处理
        }
    }
}