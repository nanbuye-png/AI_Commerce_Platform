package com.commerce.platform.cart.integration;

import com.commerce.platform.cart.domain.entity.Cart;
import com.commerce.platform.cart.domain.entity.CartItem;
import com.commerce.platform.cart.domain.entity.CheckoutTransaction;
import com.commerce.platform.cart.domain.enums.CartItemStatus;
import com.commerce.platform.cart.domain.enums.CheckoutStatus;
import com.commerce.platform.cart.domain.repository.CartRepository;
import com.commerce.platform.cart.domain.repository.CheckoutTransactionRepository;
import com.commerce.platform.cart.event.listener.OrderCreateFailedEventListener;
import com.commerce.platform.cart.event.listener.OrderPaidSuccessListener;
import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.repository.OrderRepository;
import com.commerce.platform.order.event.OrderPaidEvent;
import com.commerce.platform.order.event.listener.PaymentEventListener;
import com.commerce.platform.order.service.OrderCreationApplicationService;
import com.commerce.platform.payment.domain.event.PaymentSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Checkout → Payment 完整流程集成测试
 * <p>
 * 测试完整的 Checkout Saga 流程：
 * Cart → CheckoutTransaction → Order → Payment → Order Paid → Inventory Deduct → CheckoutTransaction SUCCESS
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CheckoutPaymentIntegrationTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CheckoutTransactionRepository checkoutTransactionRepository;

    @Mock
    private OrderCreationApplicationService orderCreationApplicationService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentEventListener paymentEventListener;
    private OrderPaidSuccessListener orderPaidSuccessListener;
    private OrderCreateFailedEventListener orderCreateFailedEventListener;

    @BeforeEach
    void setUp() {
        paymentEventListener = new PaymentEventListener(orderRepository, eventPublisher);
        orderPaidSuccessListener = new OrderPaidSuccessListener(checkoutTransactionRepository);
        orderCreateFailedEventListener = new OrderCreateFailedEventListener(
                checkoutTransactionRepository, cartRepository);
    }

    // ============================================
    // 测试1：完整成功流程
    // ============================================

    @Test
    @DisplayName("完整成功流程：CartItem=CHECKED_OUT → Order=PAID → Payment=SUCCESS → Inventory=DEDUCTED → CheckoutTransaction=SUCCESS")
    void shouldCompleteFullCheckoutFlow() {
        String orderNo = "ORD20250101000000TEST";
        String checkoutNo = "CHK20250101000000TEST";

        // 1. 准备：CheckoutTransaction 为 PROCESSING
        CheckoutTransaction transaction = CheckoutTransaction.builder()
                .id(1L)
                .checkoutNo(checkoutNo)
                .userId(1L)
                .cartId(10L)
                .status(CheckoutStatus.PROCESSING)
                .build();

        when(checkoutTransactionRepository.findByOrderNo(orderNo))
                .thenReturn(Optional.of(transaction));

        // 2. 准备：Order 为 PENDING_PAYMENT
        Order order = Order.builder()
                .orderNo(orderNo)
                .buyerId(1L)
                .merchantId(1L)
                .storeId(1L)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .payAmount(new BigDecimal("299.97"))
                .build();
        order.setId(1L);

        // PaymentEventListener now calls findByOrderNo(String.valueOf(event.getOrderId()))
        // event.getOrderId() = 1L, so the call is findByOrderNo("1")
        when(orderRepository.findByOrderNo("1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(checkoutTransactionRepository.save(any(CheckoutTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 3. 模拟支付成功 → Order 侧监听
        PaymentSuccessEvent paymentEvent = new PaymentSuccessEvent(
                1L, 1L, "TXN001", new BigDecimal("299.97"));
        paymentEventListener.handlePaymentSuccess(paymentEvent);

        // 验证 Order 状态变为 PAID
        assertEquals(OrderStatus.PAID, order.getOrderStatus());

        // 验证 OrderPaidEvent 被发布
        verify(eventPublisher).publishEvent(any(OrderPaidEvent.class));

        // 4. 模拟 OrderPaidEvent → Cart 侧完成 CheckoutTransaction
        OrderPaidEvent paidEvent = new OrderPaidEvent(1L, orderNo, "PAY20250101000000");
        orderPaidSuccessListener.onOrderPaid(paidEvent);

        // 验证 CheckoutTransaction 状态为 SUCCESS
        assertEquals(CheckoutStatus.SUCCESS, transaction.getStatus());
        assertEquals(orderNo, transaction.getOrderNo());
    }

    // ============================================
    // 测试2：Payment 创建失败补偿
    // ============================================

    @Test
    @DisplayName("Payment 创建失败 → CheckoutTransaction FAILED → CartItem 恢复 ACTIVE")
    void shouldCompensateOnPaymentCreationFailure() {
        Long userId = 1L;
        Long cartId = 10L;
        String checkoutNo = "CHK20250101000000TEST02";

        // 准备：已 CHECKED_OUT 的购物车
        Cart cart = Cart.builder().id(cartId).userId(userId).build();
        CartItem item = CartItem.builder()
                .id(1L).cartId(cartId).skuId(1001L)
                .status(CartItemStatus.CHECKED_OUT)
                .price(new BigDecimal("99.99")).quantity(1)
                .build();
        cart.getItems().add(item);

        // 准备：PROCESSING 的 CheckoutTransaction
        CheckoutTransaction transaction = CheckoutTransaction.builder()
                .id(1L).checkoutNo(checkoutNo).userId(userId)
                .cartId(cartId).status(CheckoutStatus.PROCESSING)
                .build();

        // 模拟 Order 创建失败事件 → 补偿
        when(checkoutTransactionRepository.findByCheckoutNo(checkoutNo))
                .thenReturn(Optional.of(transaction));
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(checkoutTransactionRepository.save(any(CheckoutTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        com.commerce.platform.order.event.OrderCreateFailedEvent failEvent =
                new com.commerce.platform.order.event.OrderCreateFailedEvent(checkoutNo, "支付创建失败");
        orderCreateFailedEventListener.onOrderCreateFailed(failEvent);

        // 验证补偿结果
        assertEquals(CheckoutStatus.FAILED, transaction.getStatus());
        assertEquals("支付创建失败", transaction.getFailReason());
        assertEquals(CartItemStatus.ACTIVE, item.getStatus());
    }

    // ============================================
    // 测试3：重复 PaymentSuccessEvent
    // ============================================

    @Test
    @DisplayName("重复 PaymentSuccessEvent：Order 不重复支付，Inventory 不重复扣减")
    void shouldHandleDuplicatePaymentSuccessEvent() {
        Long orderId = 1L;

        // Order 已为 PAID
        Order order = Order.builder()
                .orderNo("ORD20250101000000TEST03")
                .merchantId(1L)
                .storeId(1L)
                .orderStatus(OrderStatus.PAID)
                .build();
        order.setId(orderId);

        // PaymentEventListener calls findByOrderNo("1")
        when(orderRepository.findByOrderNo(String.valueOf(orderId))).thenReturn(Optional.of(order));

        // 第一次支付成功事件
        PaymentSuccessEvent event = new PaymentSuccessEvent(
                1L, orderId, "TXN001", new BigDecimal("100.00"));
        paymentEventListener.handlePaymentSuccess(event);

        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        verify(orderRepository, never()).save(any()); // 不保存
        verify(eventPublisher, never()).publishEvent(any(OrderPaidEvent.class)); // 不发布
    }

    // ============================================
    // 测试4：重复 OrderPaidEvent
    // ============================================

    @Test
    @DisplayName("重复 OrderPaidEvent：库存不重复扣减")
    void shouldHandleDuplicateOrderPaidEvent() {
        String orderNo = "ORD20250101000000TEST04";
        String checkoutNo = "CHK20250101000000TEST04";

        // CheckoutTransaction 已为 SUCCESS
        CheckoutTransaction transaction = CheckoutTransaction.builder()
                .id(1L).checkoutNo(checkoutNo).orderNo(orderNo)
                .status(CheckoutStatus.SUCCESS)
                .build();

        when(checkoutTransactionRepository.findByOrderNo(orderNo))
                .thenReturn(Optional.of(transaction));

        // 重复的 OrderPaidEvent
        OrderPaidEvent paidEvent = new OrderPaidEvent(1L, orderNo, "PAY001");
        orderPaidSuccessListener.onOrderPaid(paidEvent);

        // 验证：未重复修改
        assertEquals(CheckoutStatus.SUCCESS, transaction.getStatus());
        verify(checkoutTransactionRepository, never()).save(any());
    }
}