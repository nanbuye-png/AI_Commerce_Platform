package com.commerce.platform.order.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.order.dto.request.AdminOrderQueryRequest;
import com.commerce.platform.order.dto.response.AdminOrderVO;
import com.commerce.platform.order.service.AdminOrderApplicationService;
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

    private final AdminOrderApplicationService adminOrderApplicationService;

    /**
     * 商家订单列表查询
     * 自动按当前登录商家的 merchantId 过滤
     */
    @GetMapping
    public Result<Page<AdminOrderVO>> listOrders(AdminOrderQueryRequest query, Authentication authentication) {
        Long merchantId = getMerchantId(authentication);
        query.setMerchantId(merchantId);
        log.info("Merchant 订单列表查询 - merchantId={}, page={}, size={}", merchantId, query.getPage(), query.getPageSize());
        Page<AdminOrderVO> orderPage = adminOrderApplicationService.queryOrders(query);
        log.info("Merchant 订单列表查询完成 - total={}", orderPage.getTotalElements());
        return Result.success(orderPage);
    }

    /**
     * 商家订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<AdminOrderVO> getOrderDetail(@PathVariable String orderNo) {
        log.info("Merchant 订单详情 - orderNo={}", orderNo);
        AdminOrderVO vo = adminOrderApplicationService.getOrderDetail(orderNo);
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