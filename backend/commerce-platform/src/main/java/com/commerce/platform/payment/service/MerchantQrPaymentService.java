package com.commerce.platform.payment.service;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.payment.domain.entity.MerchantQrPayment;
import com.commerce.platform.payment.domain.entity.MerchantQrPaymentStatus;
import com.commerce.platform.payment.domain.repository.MerchantQrPaymentRepository;
import com.commerce.platform.payment.event.PaymentCompletedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantQrPaymentService {

    public static final long EXPIRE_MINUTES = 15;

    private final MerchantQrPaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackOn = Exception.class)
    public void acceptOrder(Long merchantId, String orderNo) {
        Order order = orderRepository.findByMerchantIdAndOrderNo(merchantId, orderNo)
                .orElseThrow(() -> new BusinessException(32004, String.format("订单不存在：%s", orderNo)));
        order.accept();
        orderRepository.save(order);
        log.info("商家接单成功 - merchantId={}, orderNo={}", merchantId, orderNo);
    }

    @Transactional(rollbackOn = Exception.class)
    public MerchantQrPayment createPayment(Long merchantId, String orderNo) {
        Order order = orderRepository.findByMerchantIdAndOrderNo(merchantId, orderNo)
                .orElseThrow(() -> new BusinessException(32004, String.format("订单不存在：%s", orderNo)));

        order.initPayment();
        orderRepository.save(order);

        paymentRepository.findByOrderNoAndStatus(orderNo, MerchantQrPaymentStatus.WAITING)
                .ifPresent(existing -> {
                    existing.markExpired();
                    paymentRepository.save(existing);
                });

        MerchantQrPayment payment = MerchantQrPayment.builder()
                .paymentNo(generatePaymentNo())
                .orderNo(orderNo)
                .buyerId(order.getBuyerId())
                .merchantId(merchantId)
                .amount(order.getPayAmount())
                .qrToken(UUID.randomUUID().toString().replace("-", ""))
                .status(MerchantQrPaymentStatus.WAITING)
                .expireTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES))
                .build();

        paymentRepository.save(payment);
        log.info("商家发起收款成功 - merchantId={}, orderNo={}, paymentNo={}, expireTime={}",
                merchantId, orderNo, payment.getPaymentNo(), payment.getExpireTime());
        return payment;
    }

    @Transactional(rollbackOn = Exception.class)
    public MerchantQrPayment getPaymentByToken(String qrToken) {
        MerchantQrPayment payment = paymentRepository.findByQrTokenAndStatus(qrToken, MerchantQrPaymentStatus.WAITING)
                .orElseThrow(() -> new BusinessException(33001, "二维码无效或已失效"));
        if (LocalDateTime.now().isAfter(payment.getExpireTime())) {
            payment.markExpired();
            paymentRepository.save(payment);
            throw new BusinessException(33002, "二维码已过期，请让商家重新发起收款");
        }
        return payment;
    }

    @Transactional(rollbackOn = Exception.class)
    public void pay(Long buyerId, String qrToken) {
        MerchantQrPayment payment = paymentRepository.findByQrTokenAndStatus(qrToken, MerchantQrPaymentStatus.WAITING)
                .orElseThrow(() -> new BusinessException(33001, "二维码无效或已失效"));
        if (!payment.getBuyerId().equals(buyerId)) {
            throw new BusinessException(33003, "无权支付该订单");
        }
        payment.markPaid();
        paymentRepository.save(payment);

        Order order = orderRepository.findByOrderNo(payment.getOrderNo())
                .orElseThrow(() -> new BusinessException(32004, String.format("订单不存在：%s", payment.getOrderNo())));
        order.pay();
        orderRepository.save(order);

        eventPublisher.publishEvent(new PaymentCompletedEvent(
                payment.getId(), order.getId(), order.getOrderNo(),
                payment.getAmount(), LocalDateTime.now()));

        log.info("二维码支付成功 - paymentNo={}, orderNo={}, buyerId={}, amount={}",
                payment.getPaymentNo(), payment.getOrderNo(), buyerId, payment.getAmount());
    }

    @Transactional(rollbackOn = Exception.class)
    public void cancel(Long buyerId, String qrToken) {
        MerchantQrPayment payment = paymentRepository.findByQrTokenAndStatus(qrToken, MerchantQrPaymentStatus.WAITING)
                .orElseThrow(() -> new BusinessException(33001, "二维码无效或已失效"));
        if (!payment.getBuyerId().equals(buyerId)) {
            throw new BusinessException(33003, "无权取消该支付");
        }
        payment.cancel();
        paymentRepository.save(payment);
        log.info("二维码支付已取消 - paymentNo={}, orderNo={}, buyerId={}",
                payment.getPaymentNo(), payment.getOrderNo(), buyerId);
    }

    public MerchantQrPayment getPaymentStatus(String orderNo) {
        return paymentRepository.findByOrderNoAndStatus(orderNo, MerchantQrPaymentStatus.WAITING)
                .orElseGet(() -> paymentRepository.findByOrderNoAndStatus(orderNo, MerchantQrPaymentStatus.PAID)
                        .orElseThrow(() -> new BusinessException(33001, "支付流水不存在")));
    }

    private String generatePaymentNo() {
        return "QR" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
