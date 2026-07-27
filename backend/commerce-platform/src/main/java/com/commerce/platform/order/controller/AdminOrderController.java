package com.commerce.platform.order.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.order.dto.request.AdminCancelOrderRequest;
import com.commerce.platform.order.dto.request.AdminCloseOrderRequest;
import com.commerce.platform.order.dto.request.AdminOrderQueryRequest;
import com.commerce.platform.order.dto.response.AdminOrderVO;
import com.commerce.platform.order.service.AdminOrderApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin 订单管理 Controller
 * <p>
 * 只允许 ADMIN 角色访问。
 * GET /api/admin/orders - 分页查询全部订单
 * GET /api/admin/orders/{orderNo} - 查询订单详情
 * POST /api/admin/orders/{orderNo}/cancel - 强制取消订单
 * POST /api/admin/orders/{orderNo}/close - 强制关闭订单
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderApplicationService adminOrderApplicationService;

    /**
     * 全平台订单查询
     * <p>
     * 支持多条件筛选：orderNo, customerId, merchantId, status, paymentStatus,
     * shippingStatus, startTime, endTime
     * 排序：createdTime DESC
     * 必须使用分页。
     * </p>
     */
    @GetMapping
    public Result<Page<AdminOrderVO>> queryOrders(AdminOrderQueryRequest query) {
        log.info("Admin 订单列表查询 - page={}, size={}", query.getPage(), query.getPageSize());

        Page<AdminOrderVO> orderPage = adminOrderApplicationService.queryOrders(query);

        log.info("Admin 订单列表查询完成 - 总数={}", orderPage.getTotalElements());
        return Result.success(orderPage);
    }

    /**
     * 订单详情查询
     */
    @GetMapping("/{orderNo}")
    public Result<AdminOrderVO> getOrderDetail(@PathVariable String orderNo) {
        log.info("Admin 订单详情查询 - orderNo={}", orderNo);

        AdminOrderVO vo = adminOrderApplicationService.getOrderDetail(orderNo);

        log.info("Admin 订单详情查询完成 - orderNo={}", orderNo);
        return Result.success(vo);
    }

    /**
     * 强制取消订单
     * <p>
     * 允许取消：PENDING_PAYMENT, PAID
     * 禁止取消：SHIPPED, COMPLETED, CANCELLED
     * </p>
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<AdminOrderVO> cancelOrder(@PathVariable String orderNo,
                                             Authentication authentication,
                                             @Valid @RequestBody AdminCancelOrderRequest request) {
        Long adminId = getAdminId(authentication);
        log.info("Admin 强制取消订单 - adminId={}, orderNo={}, reason={}",
                adminId, orderNo, request.getCancelReason());

        AdminOrderVO vo = adminOrderApplicationService.cancelOrder(orderNo, adminId, request);

        log.info("Admin 强制取消订单完成 - orderNo={}", orderNo);
        return Result.success(vo);
    }

    /**
     * 强制关闭订单
     * <p>
     * 允许关闭：CANCELLED 或异常订单状态
     * </p>
     */
    @PostMapping("/{orderNo}/close")
    public Result<AdminOrderVO> closeOrder(@PathVariable String orderNo,
                                            Authentication authentication,
                                            @RequestBody(required = false) AdminCloseOrderRequest request) {
        Long adminId = getAdminId(authentication);
        if (request == null) {
            request = new AdminCloseOrderRequest();
        }
        log.info("Admin 强制关闭订单 - adminId={}, orderNo={}, reason={}",
                adminId, orderNo, request.getCloseReason());

        AdminOrderVO vo = adminOrderApplicationService.closeOrder(orderNo, adminId, request);

        log.info("Admin 强制关闭订单完成 - orderNo={}", orderNo);
        return Result.success(vo);
    }

    /**
     * 从 Authentication 中获取 Admin ID
     */
    private Long getAdminId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}