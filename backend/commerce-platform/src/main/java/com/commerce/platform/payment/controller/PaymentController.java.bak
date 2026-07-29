package com.commerce.platform.payment.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.payment.dto.request.CreatePaymentRequest;
import com.commerce.platform.payment.dto.request.PaymentCallbackRequest;
import com.commerce.platform.payment.dto.response.PaymentResultVO;
import com.commerce.platform.payment.dto.response.PaymentVO;
import com.commerce.platform.payment.service.PaymentApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 支付 Controller
 * <p>
 * 提供支付创建、发起支付、支付回调、支付查询接口。
 * 只允许登录用户访问。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    /**
     * 创建支付
     * <p>
     * POST /api/payments
     * 登录用户创建支付记录。
     * </p>
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT', 'ADMIN')")
    public Result<PaymentVO> createPayment(Authentication authentication,
                                            @Valid @RequestBody CreatePaymentRequest request) {
        Long userId = getUserId(authentication);
        log.info("创建支付 - userId={}, orderNo={}", userId, request.getOrderNo());

        PaymentVO vo = paymentApplicationService.createPayment(request, userId);

        log.info("创建支付完成 - paymentNo={}", vo.getPaymentNo());
        return Result.success(vo);
    }

    /**
     * 发起支付
     * <p>
     * POST /api/payments/{paymentNo}/pay
     * 将支付状态从 CREATED 变为 PENDING，并调用支付 Provider。
     * </p>
     */
    @PostMapping("/{paymentNo}/pay")
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT', 'ADMIN')")
    public Result<PaymentResultVO> pay(@PathVariable String paymentNo) {
        log.info("发起支付 - paymentNo={}", paymentNo);

        PaymentResultVO result = paymentApplicationService.pay(paymentNo);

        log.info("发起支付完成 - paymentNo={}, status={}", paymentNo, result.getPaymentStatus());
        return Result.success(result);
    }

    /**
     * 支付成功回调（Mock）
     * <p>
     * POST /api/payments/{paymentNo}/callback
     * 模拟第三方支付回调通知。
     * </p>
     */
    @PostMapping("/{paymentNo}/callback")
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT', 'ADMIN')")
    public Result<PaymentVO> handleCallback(@PathVariable String paymentNo,
                                             @Valid @RequestBody PaymentCallbackRequest request) {
        log.info("支付回调 - paymentNo={}, transactionNo={}", paymentNo, request.getTransactionNo());

        PaymentVO vo = paymentApplicationService.handlePaymentSuccess(paymentNo, request.getTransactionNo());

        log.info("支付回调完成 - paymentNo={}", paymentNo);
        return Result.success(vo);
    }

    /**
     * 查询支付详情
     * <p>
     * GET /api/payments/{paymentNo}
     * </p>
     */
    @GetMapping("/{paymentNo}")
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT', 'ADMIN')")
    public Result<PaymentVO> getPayment(@PathVariable String paymentNo) {
        log.info("查询支付详情 - paymentNo={}", paymentNo);

        PaymentVO vo = paymentApplicationService.getPaymentByPaymentNo(paymentNo);

        log.info("查询支付详情完成 - paymentNo={}, status={}", paymentNo, vo.getPaymentStatus());
        return Result.success(vo);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}