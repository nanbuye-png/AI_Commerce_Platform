package com.commerce.platform.order.service;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.dto.request.CreateOrderRequest;
import com.commerce.platform.order.dto.request.OrderQueryRequest;
import com.commerce.platform.order.dto.response.CreateOrderResponse;
import com.commerce.platform.order.dto.response.OrderVO;
import com.commerce.platform.payment.service.MerchantQrPaymentService;
import com.commerce.platform.product.entity.ProductSku;
import com.commerce.platform.product.repository.ProductSkuRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单应用服务
 * <p>
 * 负责事务编排、跨域服务调用、异常回滚、订单查询。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final ProductSkuRepository productSkuRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final MerchantQrPaymentService merchantQrPaymentService;

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderResponse placeOrder(CreateOrderRequest request, Long buyerId) {
        long startTime = System.currentTimeMillis();

        List<Long> skuIds = request.getItems().stream()
                .map(com.commerce.platform.order.dto.request.CreateOrderItemRequest::getSkuId)
                .collect(Collectors.toList());

        List<ProductSku> skus = productSkuRepository.findAllById(skuIds);
        if (skus.isEmpty()) {
            throw new BusinessException(32001, "商品不存在");
        }

        Map<Long, ProductSku> skuMap = new HashMap<>();
        for (ProductSku sku : skus) {
            skuMap.put(sku.getId(), sku);
        }

        for (Long skuId : skuIds) {
            if (!skuMap.containsKey(skuId)) {
                throw new BusinessException(32001, String.format("商品不存在：SKU=%d", skuId));
            }
        }

        for (ProductSku sku : skus) {
            if (!"ACTIVE".equals(sku.getStatus())) {
                throw new BusinessException(32002, String.format("商品已下架：SKU=%d", sku.getId()));
            }
        }

        // 库存校验：购买数量必须 <= 可用库存
        for (com.commerce.platform.order.dto.request.CreateOrderItemRequest itemReq : request.getItems()) {
            Long skuId = itemReq.getSkuId();
            int quantity = itemReq.getQuantity();
            InventoryStock stock = inventoryStockRepository.findBySkuId(skuId)
                    .orElse(null);
            int available = stock != null ? stock.getAvailableQuantity() : 0;
            if (available < quantity) {
                throw new BusinessException(32003,
                        String.format("商品库存不足：SKU=%d，当前库存=%d，购买数量=%d", skuId, available, quantity));
            }
        }

        ProductSku firstSku = skus.get(0);
        Long merchantId = firstSku.getProduct().getMerchantId();
        Long storeId = firstSku.getProduct().getStoreId();

        Order order = orderDomainService.createOrder(request, skuMap, buyerId, merchantId, storeId);
        orderDomainService.setOrderAddress(order,
                "收件人姓名", "13800138000",
                "广东省", "深圳市", "南山区",
                "科技园南区A栋", "518000");
        order = orderRepository.save(order);

        // 下单成功 → 自动接单 → 自动生成商户收款二维码（记录 qrToken，供用户端"去付款"）
        try {
            merchantQrPaymentService.acceptOrder(merchantId, order.getOrderNo());
            merchantQrPaymentService.createPayment(merchantId, order.getOrderNo());
            log.info("订单已自动接单并生成收款二维码 - orderNo={}, merchantId={}", order.getOrderNo(), merchantId);
        } catch (Exception e) {
            // 自动接单/收款失败不应阻塞下单主流程，仅记录日志（商家仍可手动接单/发起收款）
            log.warn("自动接单/发起收款失败，可手动处理 - orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("订单创建成功 - orderNo={}, customerId={}, amount={}, 耗时={}ms",
                order.getOrderNo(), buyerId, order.getPayAmount(), elapsed);

        return CreateOrderResponse.builder()
                .orderNo(order.getOrderNo())
                .payAmount(order.getPayAmount())
                .orderStatus(order.getOrderStatus().name())
                .createdTime(order.getCreatedTime())
                .build();
    }

    /**
     * 查询我的订单列表
     * <p>
     * 必须开启只读事务，否则 open-in-view=false 时访问懒加载
     * items/address 会抛出 LazyInitializationException。
     * </p>
     */
    @Transactional(readOnly = true)
    public Page<OrderVO> getMyOrders(Long buyerId, OrderQueryRequest query) {
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
            orderPage = orderRepository.findByBuyerIdAndOrderStatusAndCreatedTimeBetween(
                    buyerId, orderStatus, startTimeObj, endTimeObj, pageable);
        } else if (hasStatus) {
            OrderStatus orderStatus = OrderStatus.valueOf(statusStr);
            orderPage = orderRepository.findByBuyerIdAndOrderStatus(buyerId, orderStatus, pageable);
        } else if (hasTime) {
            orderPage = orderRepository.findByBuyerIdAndCreatedTimeBetween(
                    buyerId, startTimeObj, endTimeObj, pageable);
        } else {
            orderPage = orderRepository.findByBuyerId(buyerId, pageable);
        }

        Page<OrderVO> voPage = orderPage.map(orderDomainService::buildOrderVO);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("查询我的订单 - customerId={}, status={}, 耗时={}ms", buyerId, statusStr, elapsed);

        return voPage;
    }

    /**
     * 查询订单详情
     */
    @Transactional(readOnly = true)
    public OrderVO getOrderDetail(Long buyerId, String orderNo) {
        long startTime = System.currentTimeMillis();

        Order order = orderRepository.findByBuyerIdAndOrderNo(buyerId, orderNo)
                .orElseThrow(() -> new BusinessException(32004, String.format("订单不存在：%s", orderNo)));

        OrderVO vo = orderDomainService.buildOrderVO(order);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("查询订单详情 - customerId={}, orderNo={}, 耗时={}ms", buyerId, orderNo, elapsed);

        return vo;
    }
}