package com.commerce.platform.payment.service;

import com.commerce.platform.payment.domain.entity.Payment;
import com.commerce.platform.payment.domain.enums.PaymentStatus;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import com.commerce.platform.payment.dto.request.CreatePaymentRequest;
import com.commerce.platform.payment.dto.response.PaymentResultVO;
import com.commerce.platform.payment.dto.response.PaymentVO;
import com.commerce.platform.payment.event.PaymentCreatedEvent;
import com.commerce.platform.payment.event.PaymentSuccessEvent;
import com.commerce.platform.payment.exception.PaymentAlreadyProcessedException;
import com.commerce.platform.payment.exception.PaymentNotFoundException;
import com.commerce.platform.payment.provider.PaymentNoGenerator;
import com.commerce.platform.payment.provider.PaymentProvider;
import com.commerce.platform.payment.provider.model.PaymentResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 支付应用服务
 * <p>
 * 负责创建支付、发起支付、支付成功处理、支付状态查询、事件发布。
 * 禁止直接修改 Payment 状态，必须调用 Payment Entity 方法。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;
    private final PaymentNoGenerator paymentNoGenerator;
    private final PaymentProvider paymentProvider;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建支付
     * <p>
     * 1. 接收请求
     * 2. 生成 paymentNo
     * 3. 创建 Payment Entity（初始状态 CREATED）
     * 4. 保存 PaymentRepository
     * 5. 发布 PaymentCreatedEvent
     * 6. 返回 PaymentVO
     * </p>
     */
    @Transactional(rollbackOn = Exception.class)
    public PaymentVO createPayment(CreatePaymentRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("创建支付 - orderNo={}, amount={}, userId={}", request.getOrderNo(), request.getAmount(), userId);

        String paymentNo = paymentNoGenerator.generate();

        Payment payment = Payment.builder()
                .paymentNo(paymentNo)
                .orderNo(request.getOrderNo())
                .userId(userId)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.CREATED)
                .build();

        payment = paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentCreatedEvent(paymentNo, request.getOrderNo(), userId));

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("创建支付成功 - paymentNo={}, orderNo={}, 耗时={}ms", paymentNo, request.getOrderNo(), elapsed);

        return buildPaymentVO(payment);
    }

    /**
     * 发起支付
     * <p>
     * 1. 查询 Payment
     * 2. 调用 Payment.startPay()
     * 3. 保存 Payment
     * 4. 调用 PaymentProvider
     * 5. 返回支付结果
     * </p>
     */
    @Transactional(rollbackOn = Exception.class)
    public PaymentResultVO pay(String paymentNo) {
        long startTime = System.currentTimeMillis();
        log.info("发起支付 - paymentNo={}", paymentNo);

        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new PaymentNotFoundException(paymentNo));

        // 领域行为：由 Entity 校验状态并转换
        payment.startPay();
        paymentRepository.save(payment);

        // 调用 Provider（不修改 Payment 状态）
        PaymentResult providerResult = paymentProvider.pay(payment);

        if (providerResult.isSuccess()) {
            // Provider 成功，更新支付状态
            payment.success(providerResult.getTransactionNo());
            paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentSuccessEvent(
                    paymentNo, payment.getOrderNo(), providerResult.getTransactionNo(), payment.getAmount()));

            log.info("支付成功 - paymentNo={}, transactionNo={}", paymentNo, providerResult.getTransactionNo());
        } else {
            // Provider 失败，不修改状态，保持 PENDING
            log.warn("支付失败 - paymentNo={}, message={}", paymentNo, providerResult.getMessage());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("发起支付完成 - paymentNo={}, 耗时={}ms", paymentNo, elapsed);

        return buildPaymentResultVO(payment);
    }

    /**
     * 支付成功回调（模拟第三方回调）
     * <p>
     * 1. 查询 Payment
     * 2. 判断状态，幂等处理
     * 3. Payment.success(transactionNo)
     * 4. 保存
     * 5. 发布 PaymentSuccessEvent
     * 6. 返回 PaymentVO
     * </p>
     */
    @Transactional(rollbackOn = Exception.class)
    public PaymentVO handlePaymentSuccess(String paymentNo, String transactionNo) {
        long startTime = System.currentTimeMillis();
        log.info("支付成功回调 - paymentNo={}, transactionNo={}", paymentNo, transactionNo);

        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new PaymentNotFoundException(paymentNo));

        // 幂等处理：如果已经是 SUCCESS，不重复处理
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            log.warn("支付已成功，忽略重复回调 - paymentNo={}", paymentNo);
            throw new PaymentAlreadyProcessedException(paymentNo);
        }

        // 领域行为：由 Entity 校验状态并转换
        payment.success(transactionNo);
        payment = paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentSuccessEvent(paymentNo, payment.getOrderNo(), transactionNo, payment.getAmount()));

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("支付成功回调完成 - paymentNo={}, transactionNo={}, 耗时={}ms",
                paymentNo, transactionNo, elapsed);

        return buildPaymentVO(payment);
    }

    /**
     * 查询支付详情
     */
    public PaymentVO getPaymentByPaymentNo(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new PaymentNotFoundException(paymentNo));
        return buildPaymentVO(payment);
    }

    /**
     * 根据订单号查询支付
     */
    public PaymentVO getPaymentByOrderNo(String orderNo) {
        Payment payment = paymentRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new PaymentNotFoundException(orderNo));
        return buildPaymentVO(payment);
    }

    private PaymentVO buildPaymentVO(Payment payment) {
        return PaymentVO.builder()
                .id(payment.getId())
                .paymentNo(payment.getPaymentNo())
                .orderNo(payment.getOrderNo())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionNo(payment.getTransactionNo())
                .createdTime(payment.getCreatedTime())
                .paidTime(payment.getPaidTime())
                .build();
    }

    private PaymentResultVO buildPaymentResultVO(Payment payment) {
        return PaymentResultVO.builder()
                .paymentNo(payment.getPaymentNo())
                .paymentStatus(payment.getPaymentStatus().name())
                .transactionNo(payment.getTransactionNo())
                .build();
    }
}