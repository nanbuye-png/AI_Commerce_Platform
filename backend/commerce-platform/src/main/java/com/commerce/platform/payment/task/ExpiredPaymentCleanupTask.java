package com.commerce.platform.payment.task;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.payment.domain.entity.MerchantQrPayment;
import com.commerce.platform.payment.domain.entity.MerchantQrPaymentStatus;
import com.commerce.platform.payment.domain.repository.MerchantQrPaymentRepository;
import com.commerce.platform.payment.service.MerchantQrPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 过期支付清理任务
 * <p>
 * 每 15 分钟执行一次：
 * 1. 将 WAITING 且已过期的支付流水标记为 EXPIRED
 * 2. 物理删除超过 15 分钟仍未支付的订单（PENDING_PAYMENT）
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredPaymentCleanupTask {

    private final MerchantQrPaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Scheduled(fixedDelayString = "${app.payment.cleanup-interval-ms:900000}", initialDelayString = "${app.payment.cleanup-initial-delay-ms:30000}")
    @Transactional
    public void cleanupExpiredPayments() {
        long start = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        // 1. 标记已过期的待支付流水
        List<MerchantQrPayment> expiredPayments = paymentRepository.findByStatusAndExpireTimeBefore(
                MerchantQrPaymentStatus.WAITING, now);
        for (MerchantQrPayment p : expiredPayments) {
            p.markExpired();
            paymentRepository.save(p);
            log.info("支付流水已过期 - paymentNo={}, orderNo={}", p.getPaymentNo(), p.getOrderNo());
        }

        // 2. 物理删除超过 15 分钟仍未支付且未接单的订单
        List<Order> expiredOrders = orderRepository.findByOrderStatusAndCreatedTimeBefore(
                OrderStatus.PENDING_PAYMENT, now.minusMinutes(MerchantQrPaymentService.EXPIRE_MINUTES));
        for (Order order : expiredOrders) {
            // 已接单或已发起支付的订单不自动删除（保留给商家继续跟进）
            if (order.getAcceptTime() != null || order.getPaymentInitTime() != null) {
                continue;
            }
            log.info("物理删除过期未支付订单 - orderNo={}, createdTime={}", order.getOrderNo(), order.getCreatedTime());
            orderRepository.delete(order);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("过期支付清理完成 - expiredPayments={}, deletedOrders={}, 耗时={}ms",
                expiredPayments.size(), expiredOrders.stream().filter(o -> o.getAcceptTime() == null && o.getPaymentInitTime() == null).count(), elapsed);
    }
}

