package com.commerce.platform.order.service;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.enums.ShippingStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.dto.request.MerchantOrderQueryRequest;
import com.commerce.platform.order.dto.request.ShipOrderRequest;
import com.commerce.platform.order.dto.response.OrderVO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 商家订单应用服务
 * <p>
 * 负责商家订单查询、发货、备注等操作。
 * 所有查询必须包含 merchantId，防止越权访问。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantOrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;

    /**
     * 查询商家订单列表
     */
    public Page<OrderVO> getMerchantOrders(Long merchantId, MerchantOrderQueryRequest query) {
        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(
                query.getPage() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<Order> orderPage;
        String statusStr = query.getStatus();
        LocalDateTime startTimeObj = query.getStartTime();
        LocalDateTime endTimeObj = query.getEndTime();

        boolean hasStatus = statusStr != null && !statusStr.isEmpty() && !"ALL".equalsIgnoreCase(statusStr);
        boolean hasTime = startTimeObj != null && endTimeObj != null;

        if (hasStatus && hasTime) {
            OrderStatus orderStatus = OrderStatus.valueOf(statusStr);
            orderPage = orderRepository.findByMerchantIdAndOrderStatusAndCreatedTimeBetween(
                    merchantId, orderStatus, startTimeObj, endTimeObj, pageable);
        } else if (hasStatus) {
            OrderStatus orderStatus = OrderStatus.valueOf(statusStr);
            orderPage = orderRepository.findByMerchantIdAndOrderStatus(merchantId, orderStatus, pageable);
        } else if (hasTime) {
            orderPage = orderRepository.findByMerchantIdAndCreatedTimeBetween(
                    merchantId, startTimeObj, endTimeObj, pageable);
        } else {
            orderPage = orderRepository.findByMerchantId(merchantId, pageable);
        }

        Page<OrderVO> voPage = orderPage.map(orderDomainService::buildOrderVO);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("商家查询订单列表 - merchantId={}, status={}, 耗时={}ms", merchantId, statusStr, elapsed);

        return voPage;
    }

    /**
     * 查询商家订单详情
     */
    public OrderVO getMerchantOrderDetail(Long merchantId, String orderNo) {
        long startTime = System.currentTimeMillis();

        Order order = orderRepository.findByMerchantIdAndOrderNo(merchantId, orderNo)
                .orElseThrow(() -> new BusinessException(32004, String.format("订单不存在：%s", orderNo)));

        OrderVO vo = orderDomainService.buildOrderVO(order);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("商家查询订单详情 - merchantId={}, orderNo={}, 耗时={}ms", merchantId, orderNo, elapsed);

        return vo;
    }

    /**
     * 商家发货
     */
    @Transactional(rollbackOn = Exception.class)
    public void shipOrder(Long merchantId, String orderNo, ShipOrderRequest request) {
        long startTime = System.currentTimeMillis();

        Order order = orderRepository.findByMerchantIdAndOrderNo(merchantId, orderNo)
                .orElseThrow(() -> new BusinessException(32004, String.format("订单不存在：%s", orderNo)));

        // 校验状态：仅 PAID 或 PROCESSING 可发货
        if (order.getOrderStatus() != OrderStatus.PAID && order.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new BusinessException(32005,
                    String.format("订单状态不允许发货：当前状态=%s", order.getOrderStatus().name()));
        }

        // 更新订单状态
        if (order.getOrderStatus() == OrderStatus.PAID) {
            order.setOrderStatus(OrderStatus.PROCESSING);
        }
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setShippingStatus(ShippingStatus.SHIPPED);
        order.setShippingTime(LocalDateTime.now());

        orderRepository.save(order);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("订单发货成功 - merchantId={}, orderNo={}, logistics={}, trackingNo={}, 耗时={}ms",
                merchantId, orderNo, request.getLogisticsCompany(), request.getTrackingNo(), elapsed);
    }

    /**
     * 商家更新备注
     */
    @Transactional(rollbackOn = Exception.class)
    public void updateMerchantRemark(Long merchantId, String orderNo, String remark) {
        long startTime = System.currentTimeMillis();

        Order order = orderRepository.findByMerchantIdAndOrderNo(merchantId, orderNo)
                .orElseThrow(() -> new BusinessException(32004, String.format("订单不存在：%s", orderNo)));

        order.setMerchantRemark(remark);
        orderRepository.save(order);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("商家备注更新成功 - merchantId={}, orderNo={}, 耗时={}ms", merchantId, orderNo, elapsed);
    }
}