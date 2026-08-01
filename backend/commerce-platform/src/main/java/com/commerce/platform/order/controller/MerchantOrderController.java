package com.commerce.platform.order.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.order.dto.request.MerchantOrderQueryRequest;
import com.commerce.platform.order.dto.response.OrderVO;
import com.commerce.platform.order.service.MerchantOrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Merchant 订单管理 Controller
 * <p>
 * 只允许 MERCHANT 角色访问。
 * 按当前登录商家 merchantId 隔离数据。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantOrderController {

    private final MerchantOrderApplicationService merchantOrderApplicationService;

    /**
     * 商家订单列表查询
     * 自动按当前登录商家的 merchantId 过滤
     */
    @GetMapping
    public Result<Page<OrderVO>> listOrders(MerchantOrderQueryRequest query, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 订单列表查询 - merchantId={}, page={}, size={}", merchantId, query.getPage(), query.getPageSize());
        Page<OrderVO> orderPage = merchantOrderApplicationService.getMerchantOrders(merchantId, query);
        log.info("Merchant 订单列表查询完成 - total={}", orderPage.getTotalElements());
        return Result.success(orderPage);
    }

    /**
     * 商家订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        log.info("Merchant 订单详情 - merchantId={}, orderNo={}", merchantId, orderNo);
        OrderVO vo = merchantOrderApplicationService.getMerchantOrderDetail(merchantId, orderNo);
        return Result.success(vo);
    }

    /**
     * 从 JWT Authentication 中提取当前商家的 merchantId
     * <p>
     * Merchant 端 JWT 的 sub = userId，对于 MERCHANT 角色，userId = merchantId。
     * </p>
     */
    private Long getMerchantId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("未认证的请求");
        }
        return (Long) authentication.getPrincipal();
    }
}