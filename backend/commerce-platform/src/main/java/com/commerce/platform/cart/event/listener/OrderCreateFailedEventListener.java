package com.commerce.platform.cart.event.listener;

import com.commerce.platform.cart.domain.entity.Cart;
import com.commerce.platform.cart.domain.entity.CartItem;
import com.commerce.platform.cart.domain.entity.CheckoutTransaction;
import com.commerce.platform.cart.domain.enums.CartItemStatus;
import com.commerce.platform.cart.domain.enums.CheckoutStatus;
import com.commerce.platform.cart.domain.repository.CartRepository;
import com.commerce.platform.cart.domain.repository.CheckoutTransactionRepository;
import com.commerce.platform.order.event.OrderCreateFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单创建失败事件监听器
 * <p>
 * 监听 OrderCreateFailedEvent，执行 Checkout Saga 补偿操作：
 * 1. 查询 CheckoutTransaction
 * 2. 标记为 FAILED
 * 3. 恢复 CartItem 状态：CHECKED_OUT → ACTIVE
 * </p>
 *
 * 不依赖任何 Order Domain 的 Repository 或 Entity。
 * 通过 Event 与 Order Domain 解耦。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateFailedEventListener {

    private final CheckoutTransactionRepository checkoutTransactionRepository;
    private final CartRepository cartRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void onOrderCreateFailed(OrderCreateFailedEvent event) {
        log.info("收到订单创建失败事件：checkoutNo={}, reason={}", event.getCheckoutNo(), event.getReason());

        // 1. 查询 CheckoutTransaction
        CheckoutTransaction transaction = checkoutTransactionRepository
                .findByCheckoutNo(event.getCheckoutNo())
                .orElse(null);

        if (transaction == null) {
            log.warn("CheckoutTransaction 不存在，可能已被清理：checkoutNo={}", event.getCheckoutNo());
            return;
        }

        // 幂等处理：如果已经 FAILED，不再重复处理
        if (transaction.getStatus() == CheckoutStatus.FAILED) {
            log.info("CheckoutTransaction 已为 FAILED 状态，跳过重复处理：checkoutNo={}", event.getCheckoutNo());
            return;
        }

        if (transaction.getStatus() != CheckoutStatus.PROCESSING) {
            log.warn("CheckoutTransaction 状态为 {}，不能执行补偿：checkoutNo={}",
                    transaction.getStatus(), event.getCheckoutNo());
            return;
        }

        // 2. 标记为 FAILED
        transaction.fail(event.getReason() != null ? event.getReason() : "订单创建失败");
        checkoutTransactionRepository.save(transaction);

        // 3. 恢复 CartItem：CHECKED_OUT → ACTIVE
        Cart cart = cartRepository.findById(transaction.getCartId()).orElse(null);
        if (cart == null) {
            log.warn("购物车不存在，无法恢复商品状态：cartId={}", transaction.getCartId());
            return;
        }

        boolean restored = false;
        for (CartItem item : cart.getItems()) {
            if (item.getStatus() == CartItemStatus.CHECKED_OUT) {
                try {
                    item.restore();
                    restored = true;
                } catch (IllegalStateException e) {
                    log.warn("商品状态异常，跳过恢复：itemId={}, status={}", item.getId(), item.getStatus());
                }
            }
        }

        if (restored) {
            cartRepository.save(cart);
            log.info("购物车商品状态已恢复：checkoutNo={}, userId={}", event.getCheckoutNo(), transaction.getUserId());
        } else {
            log.info("购物车中没有需要恢复的 CHECKED_OUT 商品：checkoutNo={}", event.getCheckoutNo());
        }
    }
}