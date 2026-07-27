package com.commerce.platform.order.event.listener;

import com.commerce.platform.cart.event.CartCheckedOutEvent;
import com.commerce.platform.order.dto.request.CheckoutCreateOrderRequest;
import com.commerce.platform.order.dto.request.CheckoutCreateOrderRequest.CheckoutItem;
import com.commerce.platform.order.event.OrderCreateFailedEvent;
import com.commerce.platform.order.event.OrderCreatedSuccessEvent;
import com.commerce.platform.order.service.OrderCreationApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCheckoutEventListener {

    private final OrderCreationApplicationService orderCreationApplicationService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCartCheckedOut(CartCheckedOutEvent event) {
        log.info("收到购物车结算事件：checkoutNo={}, cartId={}, userId={}",
                event.getCheckoutNo(), event.getCartId(), event.getUserId());

        try {
            // 创建订单
            CheckoutCreateOrderRequest request = new CheckoutCreateOrderRequest();
            request.setUserId(event.getUserId());
            request.setAddressId(0L);

            var itemRequests = event.getItems().stream().map(item -> {
                CheckoutItem itemReq = new CheckoutItem();
                itemReq.setSkuId(item.getSkuId());
                itemReq.setProductId(item.getProductId());
                itemReq.setProductName(item.getProductName());
                itemReq.setPrice(item.getPrice());
                itemReq.setQuantity(item.getQuantity());
                return itemReq;
            }).collect(Collectors.toList());

            request.setItems(itemRequests);

            String orderNo = orderCreationApplicationService.createOrder(request);
            log.info("购物车结算订单创建成功：checkoutNo={}, orderNo={}", event.getCheckoutNo(), orderNo);

            // 订单创建成功 → 发布 OrderCreatedSuccessEvent
            eventPublisher.publishEvent(new OrderCreatedSuccessEvent(
                    orderNo, event.getCheckoutNo(), event.getUserId()));

        } catch (Exception e) {
            log.error("购物车结算订单创建失败：checkoutNo={}, error={}",
                    event.getCheckoutNo(), e.getMessage(), e);

            // 订单创建失败 → 发布 OrderCreateFailedEvent
            eventPublisher.publishEvent(new OrderCreateFailedEvent(
                    event.getCheckoutNo(), e.getMessage()));
        }
    }
}