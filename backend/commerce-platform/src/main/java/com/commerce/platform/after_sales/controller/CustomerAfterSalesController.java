package com.commerce.platform.after_sales.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.refund.domain.aggregate.Refund;
import com.commerce.platform.refund.domain.repository.RefundRepository;
import com.commerce.platform.refund.domain.valueobject.RefundReason;
import com.commerce.platform.refund.domain.valueobject.RefundStatus;
import com.commerce.platform.refund.dto.CustomerRefundRequest;
import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.repository.ReturnRepository;
import com.commerce.platform.returns.domain.valueobject.ReturnReason;
import com.commerce.platform.returns.dto.CustomerReturnRequest;
import com.commerce.platform.returns.dto.ReturnShipmentRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/after-sales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerAfterSalesController {

    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final ReturnRepository returnRepository;

    @GetMapping("/refunds")
    public Result<List<Refund>> refunds(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(refundRepository.findByUserId(userId));
    }

    @GetMapping("/returns")
    public Result<List<ReturnRequest>> returns(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(returnRepository.findByUserId(userId));
    }

    @PostMapping("/orders/{orderNo}/refund")
    @Transactional(rollbackOn = Exception.class)
    public Result<Refund> createRefund(Authentication authentication,
                                       @PathVariable String orderNo,
                                       @Valid @RequestBody CustomerRefundRequest request) {
        Long userId = getUserId(authentication);
        Order order = findOwnedOrder(userId, orderNo);
        ensureOrderCanApply(order);
        ensureNoActiveRefund(order.getId());
        order.requestRefund();
        orderRepository.save(order);
        Refund refund = Refund.create(order.getId(), userId, order.getPayAmount(), parseRefundReason(request.getReason()));
        return Result.success(refundRepository.save(refund));
    }

    @PostMapping("/orders/{orderNo}/return")
    @Transactional(rollbackOn = Exception.class)
    public Result<ReturnRequest> createReturn(Authentication authentication,
                                              @PathVariable String orderNo,
                                              @Valid @RequestBody CustomerReturnRequest request) {
        Long userId = getUserId(authentication);
        Order order = findOwnedOrder(userId, orderNo);
        ensureOrderCanApply(order);
        if (!returnRepository.findByOrderId(order.getId()).isEmpty()) {
            throw new IllegalStateException("该订单已经存在退货申请");
        }
        order.requestRefund();
        orderRepository.save(order);
        ReturnRequest returnRequest = ReturnRequest.create(order.getId(), userId, parseReturnReason(request.getReason()));
        return Result.success(returnRepository.save(returnRequest));
    }

    @PostMapping("/returns/{id}/ship")
    @Transactional(rollbackOn = Exception.class)
    public Result<ReturnRequest> submitReturnShipment(Authentication authentication,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody ReturnShipmentRequest request) {
        Long userId = getUserId(authentication);
        ReturnRequest returnRequest = returnRepository.findById(id)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("退货申请不存在"));
        returnRequest.beginReturn(request.getLogisticsCompany(), request.getTrackingNo());
        return Result.success(returnRepository.save(returnRequest));
    }

    private Order findOwnedOrder(Long userId, String orderNo) {
        return orderRepository.findByBuyerIdAndOrderNo(userId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }

    private void ensureOrderCanApply(Order order) {
        switch (order.getOrderStatus()) {
            case PAID, PROCESSING, SHIPPED, COMPLETED -> { }
            default -> throw new IllegalStateException("当前订单状态不支持售后申请");
        }
    }

    private void ensureNoActiveRefund(Long orderId) {
        if (refundRepository.findByOrderId(orderId).stream()
                .anyMatch(item -> item.getStatus() != RefundStatus.REJECTED && item.getStatus() != RefundStatus.FAILED)) {
            throw new IllegalStateException("该订单已经存在退款申请");
        }
    }

    private RefundReason parseRefundReason(String reason) {
        try { return RefundReason.valueOf(reason.toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("无效的退款原因"); }
    }

    private ReturnReason parseReturnReason(String reason) {
        try { return ReturnReason.valueOf(reason.toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("无效的退货原因"); }
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("未认证的请求");
        }
        return (Long) authentication.getPrincipal();
    }
}
