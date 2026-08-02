package com.commerce.platform.payment.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.payment.domain.entity.MerchantQrPayment;
import com.commerce.platform.payment.dto.CreatePaymentResponse;
import com.commerce.platform.payment.service.MerchantQrPaymentService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 商家二维码收款 Controller
 * <p>
 * 商家接单 → 发起收款（生成二维码）→ 用户扫码支付
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/payments")
@RequiredArgsConstructor
public class MerchantQrPaymentController {

    private final MerchantQrPaymentService paymentService;

    /**
     * 商家接单
     */
    @PostMapping("/orders/{orderNo}/accept")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> acceptOrder(@PathVariable String orderNo, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        paymentService.acceptOrder(merchantId, orderNo);
        return Result.success(null);
    }

    /**
     * 商家发起收款（生成商户二维码）
     */
    @PostMapping("/orders/{orderNo}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<CreatePaymentResponse> createPayment(@PathVariable String orderNo, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        MerchantQrPayment payment = paymentService.createPayment(merchantId, orderNo);
        CreatePaymentResponse response = CreatePaymentResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .orderNo(payment.getOrderNo())
                .amount(payment.getAmount())
                .qrToken(payment.getQrToken())
                .qrContent("AI-COMMERCE-PAY:" + payment.getQrToken())
                .expireTime(payment.getExpireTime())
                .build();
        return Result.success(response);
    }

    private Long getMerchantId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("未认证的请求");
        }
        return (Long) authentication.getPrincipal();
    }
}