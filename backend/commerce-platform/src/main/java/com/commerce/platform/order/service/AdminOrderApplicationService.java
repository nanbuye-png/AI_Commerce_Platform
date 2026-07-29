package com.commerce.platform.order.service;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.enums.PaymentStatus;
import com.commerce.platform.order.domain.enums.ShippingStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.dto.request.AdminCancelOrderRequest;
import com.commerce.platform.order.dto.request.AdminCloseOrderRequest;
import com.commerce.platform.order.dto.request.AdminOrderQueryRequest;
import com.commerce.platform.order.dto.response.AdminOrderVO;
import com.commerce.platform.order.event.OrderCancelledEvent;
import com.commerce.platform.order.event.OrderClosedEvent;
import com.commerce.platform.order.exception.OrderNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin 订单应用服务
 * <p>
 * 负责 Admin 端订单查询、详情、强制取消、强制关闭。
 * Controller 不允许直接访问 Repository。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Admin 分页查询全部订单
     */
    public Page<AdminOrderVO> queryOrders(AdminOrderQueryRequest query) {
        long startTime = System.currentTimeMillis();
        log.info("Admin 查询订单列表 - query={}", query);

        Pageable pageable = PageRequest.of(
                query.getPage() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdTime"));

        Specification<Order> spec = buildSpecification(query);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        Page<AdminOrderVO> voPage = orderPage.map(orderDomainService::buildAdminOrderVO);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Admin 查询订单列表完成 - 总数={}, 耗时={}ms", orderPage.getTotalElements(), elapsed);

        return voPage;
    }

    /**
     * Admin 查询订单详情
     */
    public AdminOrderVO getOrderDetail(String orderNo) {
        long startTime = System.currentTimeMillis();
        log.info("Admin 查询订单详情 - orderNo={}", orderNo);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new OrderNotFoundException(orderNo));

        AdminOrderVO vo = orderDomainService.buildAdminOrderVO(order);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Admin 查询订单详情完成 - orderNo={}, 耗时={}ms", orderNo, elapsed);

        return vo;
    }

    /**
     * Admin 强制取消订单
     */
    @Transactional(rollbackOn = Exception.class)
    public AdminOrderVO cancelOrder(String orderNo, Long adminId, AdminCancelOrderRequest request) {
        long startTime = System.currentTimeMillis();
        String cancelReason = request.getCancelReason();
        log.info("Admin 强制取消订单 - adminId={}, orderNo={}, reason={}", adminId, orderNo, cancelReason);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new OrderNotFoundException(orderNo));

        Long buyerId = order.getBuyerId();
        order.cancel();
        order = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCancelledEvent(order.getId(), orderNo, buyerId, adminId, cancelReason));

        log.info("Admin 强制取消订单完成 - adminId={}, orderNo={}, reason={}", adminId, orderNo, cancelReason);
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Admin 强制取消订单耗时={}ms", elapsed);

        return orderDomainService.buildAdminOrderVO(order);
    }

    /**
     * Admin 强制关闭订单
     */
    @Transactional(rollbackOn = Exception.class)
    public AdminOrderVO closeOrder(String orderNo, Long adminId, AdminCloseOrderRequest request) {
        long startTime = System.currentTimeMillis();
        String closeReason = request.getCloseReason();
        log.info("Admin 强制关闭订单 - adminId={}, orderNo={}, reason={}", adminId, orderNo, closeReason);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new OrderNotFoundException(orderNo));

        order.close();
        order = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderClosedEvent(orderNo, adminId, closeReason));

        log.info("Admin 强制关闭订单完成 - adminId={}, orderNo={}", adminId, orderNo);
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Admin 强制关闭订单耗时={}ms", elapsed);

        return orderDomainService.buildAdminOrderVO(order);
    }

    /**
     * 构建动态查询 Specification
     */
    private Specification<Order> buildSpecification(AdminOrderQueryRequest query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getOrderNo() != null && !query.getOrderNo().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("orderNo"), query.getOrderNo()));
            }
            if (query.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("buyerId"), query.getCustomerId()));
            }
            if (query.getMerchantId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("merchantId"), query.getMerchantId()));
            }
            if (query.getStatus() != null && !query.getStatus().isEmpty() && !"ALL".equalsIgnoreCase(query.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus"),
                        OrderStatus.valueOf(query.getStatus())));
            }
            if (query.getPaymentStatus() != null && !query.getPaymentStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"),
                        PaymentStatus.valueOf(query.getPaymentStatus())));
            }
            if (query.getShippingStatus() != null && !query.getShippingStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("shippingStatus"),
                        ShippingStatus.valueOf(query.getShippingStatus())));
            }
            if (query.getStartTime() != null && query.getEndTime() != null) {
                predicates.add(criteriaBuilder.between(root.get("createdTime"),
                        query.getStartTime(), query.getEndTime()));
            }
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}