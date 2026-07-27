package com.commerce.platform.payment.service;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.payment.domain.entity.Payment;
import com.commerce.platform.payment.domain.enums.PaymentMethod;
import com.commerce.platform.payment.domain.enums.PaymentStatus;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import com.commerce.platform.payment.exception.PaymentAlreadyProcessedException;
import com.commerce.platform.payment.provider.PaymentNoGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 完整支付流程集成测试
 * <p>
 * 测试链路：Create Payment → startPay → MockProvider → success
 * 注：Order PAID 状态变化由 EventListener 异步触发，已在 PaymentOrderIntegrationTest 中验证。
 * 本测试专注验证 Payment 自身状态流转的完整性。
 * </p>
 */
@SpringBootTest
class PaymentFullFlowIntegrationTest {

    @Autowired
    private PaymentApplicationService paymentApplicationService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentNoGenerator paymentNoGenerator;

    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("完整流程：创建支付 → 发起支付 → Provider成功 → Payment SUCCESS")
    void shouldCompleteFullPaymentFlow() {
        String orderNo = "FULL_FLOW_" + System.currentTimeMillis();

        orderRepository.save(Order.builder()
                .orderNo(orderNo)
                .buyerId(1L).merchantId(1L).storeId(1L)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.valueOf(100)).productAmount(BigDecimal.valueOf(100))
                .payAmount(BigDecimal.valueOf(100))
                .build());

        var createReq = new com.commerce.platform.payment.dto.request.CreatePaymentRequest();
        createReq.setOrderNo(orderNo);
        createReq.setAmount(BigDecimal.valueOf(100));
        createReq.setPaymentMethod(PaymentMethod.MOCK);
        paymentApplicationService.createPayment(createReq, 1L);

        var paymentOpt = paymentRepository.findByOrderNo(orderNo);
        assertTrue(paymentOpt.isPresent());
        String paymentNo = paymentOpt.get().getPaymentNo();
        var result = paymentApplicationService.pay(paymentNo);

        // 验证 Payment 状态
        var paymentEntity = paymentRepository.findByPaymentNo(paymentNo);
        assertTrue(paymentEntity.isPresent());
        assertEquals(PaymentStatus.SUCCESS, paymentEntity.get().getPaymentStatus(),
                "支付状态应为 SUCCESS");
        assertNotNull(paymentEntity.get().getTransactionNo(), "transactionNo 应存在");
        assertEquals("SUCCESS", result.getPaymentStatus(),
                "返回状态应为 SUCCESS");
    }

    @Test
    @DisplayName("重复支付：第二次 success() 应抛出 PaymentAlreadyProcessedException")
    void shouldThrowExceptionOnDuplicatePaymentSuccess() {
        String paymentNo = paymentNoGenerator.generate();
        paymentRepository.save(Payment.builder()
                .paymentNo(paymentNo)
                .orderNo("DUP_" + System.currentTimeMillis())
                .userId(1L).amount(BigDecimal.valueOf(100))
                .paymentMethod(PaymentMethod.MOCK)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionNo("TXN_UNIQUE_" + System.currentTimeMillis())
                .build());

        assertThrows(PaymentAlreadyProcessedException.class,
                () -> paymentApplicationService.handlePaymentSuccess(
                        paymentNo, "TXN_002_" + System.currentTimeMillis()),
                "重复支付应抛出 PaymentAlreadyProcessedException");
    }
}