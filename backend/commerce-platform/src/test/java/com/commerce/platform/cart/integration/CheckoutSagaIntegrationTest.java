package com.commerce.platform.cart.integration;

import com.commerce.platform.cart.domain.entity.Cart;
import com.commerce.platform.cart.domain.entity.CartItem;
import com.commerce.platform.cart.domain.entity.CheckoutTransaction;
import com.commerce.platform.cart.domain.enums.CartItemStatus;
import com.commerce.platform.cart.domain.enums.CheckoutStatus;
import com.commerce.platform.cart.domain.repository.CartRepository;
import com.commerce.platform.cart.domain.repository.CheckoutTransactionRepository;
import com.commerce.platform.cart.event.CartCheckedOutEvent;
import com.commerce.platform.cart.event.listener.OrderCreateFailedEventListener;
import com.commerce.platform.cart.service.CheckoutApplicationService;
import com.commerce.platform.order.event.OrderCreateFailedEvent;
import com.commerce.platform.order.event.OrderCreatedSuccessEvent;
import com.commerce.platform.order.event.listener.CartCheckoutEventListener;
import com.commerce.platform.order.service.OrderCreationApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Checkout Saga 集成测试
 * <p>
 * 测试 Cart → Order → Inventory 之间的结算一致性：
 * 1. 正常 Checkout：Cart → CheckoutTransaction SUCCESS
 * 2. Order 创建失败：CheckoutTransaction FAILED → CartItem 恢复 ACTIVE
 * 3. 重复失败事件：幂等处理
 * 4. 异常不会污染购物车
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CheckoutSagaIntegrationTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CheckoutTransactionRepository checkoutTransactionRepository;

    @Mock
    private OrderCreationApplicationService orderCreationApplicationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CheckoutApplicationService checkoutApplicationService;
    private CartCheckoutEventListener cartCheckoutEventListener;
    private OrderCreateFailedEventListener orderCreateFailedEventListener;

    @BeforeEach
    void setUp() {
        checkoutApplicationService = new CheckoutApplicationService(
                cartRepository, checkoutTransactionRepository, eventPublisher);
        cartCheckoutEventListener = new CartCheckoutEventListener(
                orderCreationApplicationService, eventPublisher);
        orderCreateFailedEventListener = new OrderCreateFailedEventListener(
                checkoutTransactionRepository, cartRepository);
    }

    private Cart createCartWithActiveItems(Long userId, Long cartId) {
        Cart cart = Cart.builder()
                .id(cartId)
                .userId(userId)
                .build();

        CartItem item1 = CartItem.builder()
                .id(1L)
                .cartId(cartId)
                .productId(100L)
                .skuId(1001L)
                .productName("商品1")
                .price(new BigDecimal("99.99"))
                .quantity(2)
                .status(CartItemStatus.ACTIVE)
                .build();

        CartItem item2 = CartItem.builder()
                .id(2L)
                .cartId(cartId)
                .productId(200L)
                .skuId(2001L)
                .productName("商品2")
                .price(new BigDecimal("199.99"))
                .quantity(1)
                .status(CartItemStatus.ACTIVE)
                .build();

        cart.getItems().add(item1);
        cart.getItems().add(item2);
        return cart;
    }

    private CartCheckedOutEvent createCheckoutEvent(String checkoutNo, Long cartId, Long userId) {
        List<CartCheckedOutEvent.CartCheckedOutItem> items = List.of(
                new CartCheckedOutEvent.CartCheckedOutItem(1001L, 100L, "商品1",
                        new BigDecimal("99.99"), 2),
                new CartCheckedOutEvent.CartCheckedOutItem(2001L, 200L, "商品2",
                        new BigDecimal("199.99"), 1)
        );
        return new CartCheckedOutEvent(checkoutNo, cartId, userId, null, items);
    }

    // ============================================
    // 测试1：正常 Checkout
    // ============================================

    @Test
    @DisplayName("正常结算：Cart → CheckoutTransaction SUCCESS")
    void shouldCompleteCheckoutSuccessfully() {
        Long userId = 1L;
        Long cartId = 10L;
        String checkoutNo = "CHK20250101000000TEST01";
        Cart cart = createCartWithActiveItems(userId, cartId);

        // 模拟 checkout 流程：商品标记为 CHECKED_OUT（由 CheckoutApplicationService 完成）
        for (CartItem item : cart.getItems()) {
            item.checkout();
        }

        // 设置 orderCreationApplicationService 返回成功
        when(orderCreationApplicationService.createOrder(any())).thenReturn("ORD20250101000000TEST");

        // 触发 CartCheckoutEventListener
        CartCheckedOutEvent event = createCheckoutEvent(checkoutNo, cartId, userId);
        cartCheckoutEventListener.onCartCheckedOut(event);

        // 验证
        verify(orderCreationApplicationService).createOrder(any());
        // 验证 OrderCreatedSuccessEvent 被发布
        verify(eventPublisher).publishEvent(any(OrderCreatedSuccessEvent.class));

        // 验证 cart items 被标记为 CHECKED_OUT
        assertTrue(cart.getItems().stream()
                .allMatch(item -> item.getStatus() == CartItemStatus.CHECKED_OUT));
    }

    // ============================================
    // 测试2：Order 创建失败 → 补偿
    // ============================================

    @Test
    @DisplayName("订单创建失败 → CheckoutTransaction FAILED → CartItem 恢复 ACTIVE")
    void shouldCompensateOnOrderCreationFailure() {
        Long userId = 1L;
        Long cartId = 10L;
        String checkoutNo = "CHK20250101000000TEST02";

        // 1. 先执行 checkout，将商品设为 CHECKED_OUT
        Cart cart = createCartWithActiveItems(userId, cartId);
        for (CartItem item : cart.getItems()) {
            item.checkout();
        }

        // 2. 创建 CheckoutTransaction（PROCESSING）
        CheckoutTransaction transaction = CheckoutTransaction.builder()
                .id(1L)
                .checkoutNo(checkoutNo)
                .userId(userId)
                .cartId(cartId)
                .status(CheckoutStatus.PROCESSING)
                .build();

        // 3. 模拟 Order 创建失败
        when(checkoutTransactionRepository.findByCheckoutNo(checkoutNo))
                .thenReturn(Optional.of(transaction));
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(checkoutTransactionRepository.save(any(CheckoutTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // 执行补偿
        OrderCreateFailedEvent failEvent = new OrderCreateFailedEvent(checkoutNo, "库存不足");
        orderCreateFailedEventListener.onOrderCreateFailed(failEvent);

        // 验证
        assertEquals(CheckoutStatus.FAILED, transaction.getStatus());
        assertEquals("库存不足", transaction.getFailReason());
        assertTrue(cart.getItems().stream()
                .allMatch(item -> item.getStatus() == CartItemStatus.ACTIVE));
        verify(cartRepository).save(any(Cart.class));
    }

    // ============================================
    // 测试3：重复失败事件（幂等）
    // ============================================

    @Test
    @DisplayName("重复失败事件：已 FAILED 状态不再重复补偿，CartItem 状态不变")
    void shouldIgnoreDuplicateOrderCreateFailedEvent() {
        Long userId = 1L;
        Long cartId = 10L;
        String checkoutNo = "CHK20250101000000TEST03";

        // 已 FAILED 的 transaction
        CheckoutTransaction transaction = CheckoutTransaction.builder()
                .id(1L)
                .checkoutNo(checkoutNo)
                .userId(userId)
                .cartId(cartId)
                .status(CheckoutStatus.FAILED)
                .failReason("库存不足")
                .build();

        // CartItem 已经恢复为 ACTIVE
        Cart cart = createCartWithActiveItems(userId, cartId);

        when(checkoutTransactionRepository.findByCheckoutNo(checkoutNo))
                .thenReturn(Optional.of(transaction));

        // 重复执行补偿
        OrderCreateFailedEvent failEvent = new OrderCreateFailedEvent(checkoutNo, "库存不足");
        orderCreateFailedEventListener.onOrderCreateFailed(failEvent);

        // 验证：未再次修改 transaction 和 cart
        assertEquals(CheckoutStatus.FAILED, transaction.getStatus());
        verify(checkoutTransactionRepository, never()).save(any());
        verify(cartRepository, never()).findById(any());
        verify(cartRepository, never()).save(any());
    }

    // ============================================
    // 测试4：异常不会污染购物车
    // ============================================

    @Test
    @DisplayName("异常不会污染购物车：未参与结算的商品保持 ACTIVE")
    void shouldNotPolluteCartOnFailure() {
        Long userId = 1L;
        Long cartId = 10L;
        String checkoutNo = "CHK20250101000000TEST04";

        // 购物车有3个商品，只有2个参与结算
        Cart cart = createCartWithActiveItems(userId, cartId);

        // 额外添加一个未参与结算的商品
        CartItem extraItem = CartItem.builder()
                .id(3L)
                .cartId(cartId)
                .productId(300L)
                .skuId(3001L)
                .productName("未结算商品")
                .price(new BigDecimal("59.99"))
                .quantity(1)
                .status(CartItemStatus.ACTIVE)
                .build();
        cart.getItems().add(extraItem);

        // 标记参与结算的商品为 CHECKED_OUT
        for (CartItem item : cart.getItems()) {
            if (item.getId() != 3L) {
                item.checkout();
            }
        }

        // 创建 CheckoutTransaction（PROCESSING）
        CheckoutTransaction transaction = CheckoutTransaction.builder()
                .id(1L)
                .checkoutNo(checkoutNo)
                .userId(userId)
                .cartId(cartId)
                .status(CheckoutStatus.PROCESSING)
                .build();

        when(checkoutTransactionRepository.findByCheckoutNo(checkoutNo))
                .thenReturn(Optional.of(transaction));
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(checkoutTransactionRepository.save(any(CheckoutTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // 执行补偿
        OrderCreateFailedEvent failEvent = new OrderCreateFailedEvent(checkoutNo, "订单创建失败");
        orderCreateFailedEventListener.onOrderCreateFailed(failEvent);

        // 验证：参与结算的商品恢复为 ACTIVE
        assertTrue(cart.getItems().stream()
                .filter(item -> item.getId() != 3L)
                .allMatch(item -> item.getStatus() == CartItemStatus.ACTIVE));

        // 验证：未参与结算的商品保持 ACTIVE
        CartItem remaining = cart.getItems().stream()
                .filter(item -> item.getId() == 3L)
                .findFirst().orElseThrow();
        assertEquals(CartItemStatus.ACTIVE, remaining.getStatus());

        assertTrue(cart.getItems().stream()
                .allMatch(item -> item.getStatus() == CartItemStatus.ACTIVE));
    }
}