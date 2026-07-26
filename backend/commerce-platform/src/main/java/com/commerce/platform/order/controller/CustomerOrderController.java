package com.commerce.platform.order.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.order.dto.request.CreateOrderRequest;
import com.commerce.platform.order.dto.response.CreateOrderResponse;
import com.commerce.platform.order.service.OrderApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * C 端订单 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final OrderApplicationService orderApplicationService;

    /**
     * 创建订单
     * <p>
     * Customer 提交下单请求，创建订单并锁定库存。
     * customerId 从 JWT Token 中获取，禁止前端传递。
     * </p>
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result<CreateOrderResponse> createOrder(Authentication authentication,
                                                    @Valid @RequestBody CreateOrderRequest request) {
        Long customerId = getCustomerId(authentication);
        log.info("创建订单 - customerId={}, items={}", customerId, request.getItems().size());

        CreateOrderResponse response = orderApplicationService.placeOrder(request, customerId);

        log.info("订单创建完成 - orderNo={}", response.getOrderNo());
        return Result.success(response);
    }

    /**
     * 从 Authentication 中获取客户 ID
     */
    private Long getCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}