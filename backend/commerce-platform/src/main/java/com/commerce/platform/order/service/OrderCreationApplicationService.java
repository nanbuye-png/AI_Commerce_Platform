package com.commerce.platform.order.service;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.entity.OrderItem;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.dto.request.CheckoutCreateOrderRequest;
import com.commerce.platform.order.dto.request.CheckoutCreateOrderRequest.CheckoutItem;
import com.commerce.platform.order.event.OrderCreatedEvent;
import com.commerce.platform.payment.event.OrderCreatedForPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreationApplicationService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public String createOrder(CheckoutCreateOrderRequest request) {
        String orderNo = generateOrderNo();

        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .orderNo(orderNo)
                .buyerId(request.getUserId())
                .merchantId(1L)
                .storeId(1L)
                .totalAmount(totalAmount)
                .productAmount(totalAmount)
                .payAmount(totalAmount)
                .freightAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .build();

        for (CheckoutItem itemReq : request.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .skuId(itemReq.getSkuId())
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .skuCode("SKU_" + itemReq.getSkuId())
                    .skuName(itemReq.getProductName())
                    .price(itemReq.getPrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();
            order.addItem(orderItem);
        }

        orderRepository.save(order);

        var orderItems = request.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemDto(item.getSkuId(), item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());
        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), orderNo, request.getUserId(), orderItems));

        // 发布订单创建待支付事件（触发 Payment 模块创建支付）
        eventPublisher.publishEvent(new OrderCreatedForPaymentEvent(
                orderNo, request.getUserId(), totalAmount, null));

        log.info("订单创建成功：orderNo={}", orderNo);
        return orderNo;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + timestamp + random;
    }
}