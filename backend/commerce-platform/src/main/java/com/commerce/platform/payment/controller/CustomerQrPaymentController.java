package com.commerce.platform.payment.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.payment.domain.entity.MerchantQrPayment;
import com.commerce.platform.payment.dto.PaymentDetailResponse;
import com.commerce.platform.payment.service.MerchantQrPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class CustomerQrPaymentController {

    private final MerchantQrPaymentService paymentService;

    @GetMapping("/qr/{qrToken}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<PaymentDetailResponse> getPaymentDetail(@PathVariable String qrToken, Authentication authentication) {
        Long buyerId = getCustomerId(authentication);
        MerchantQrPayment payment = paymentService.getPaymentByToken(qrToken);
        PaymentDetailResponse response = PaymentDetailResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .orderNo(payment.getOrderNo())
                .amount(payment.getAmount())
                .expireTime(payment.getExpireTime())
                .status(payment.getStatus().name())
                .qrToken(payment.getQrToken())
                .build();
        return Result.success(response);
    }

    @PostMapping("/qr/{qrToken}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> pay(@PathVariable String qrToken, Authentication authentication) {
        Long buyerId = getCustomerId(authentication);
        paymentService.pay(buyerId, qrToken);
        return Result.success(null);
    }

    @PostMapping("/qr/{qrToken}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> cancel(@PathVariable String qrToken, Authentication authentication) {
        Long buyerId = getCustomerId(authentication);
        paymentService.cancel(buyerId, qrToken);
        return Result.success(null);
    }

    @GetMapping("/orders/{orderNo}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MERCHANT')")
    public Result<PaymentDetailResponse> paymentStatus(@PathVariable String orderNo) {
        MerchantQrPayment payment = paymentService.getPaymentStatus(orderNo);
        PaymentDetailResponse response = PaymentDetailResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .orderNo(payment.getOrderNo())
                .amount(payment.getAmount())
                .expireTime(payment.getExpireTime())
                .status(payment.getStatus().name())
                .qrToken(payment.getQrToken())
                .build();
        return Result.success(response);
    }

    private Long getCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("未认证的请求");
        }
        return (Long) authentication.getPrincipal();
    }
}
