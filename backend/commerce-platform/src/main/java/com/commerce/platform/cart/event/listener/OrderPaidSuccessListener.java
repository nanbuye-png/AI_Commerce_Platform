package com.commerce.platform.cart.event.listener;

import com.commerce.platform.cart.domain.entity.CheckoutTransaction;
import com.commerce.platform.cart.domain.enums.CheckoutStatus;
import com.commerce.platform.cart.domain.repository.CheckoutTransactionRepository;
import com.commerce.platform.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单支付成功监听器（属于 Cart Domain）
 * <p>
 * 监听 OrderPaidEvent，完成 CheckoutTransaction 最终状态：
 * 1. 查询 CheckoutTransaction（通过 orderNo）
 * 2. 调用 checkoutTransaction.success(orderNo) → PROCESSING → SUCCESS
 * 3. 保存
 * </p>
 *
 * 不依赖 Order Entity，通过 Event 解耦。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidSuccessListener {

    private final CheckoutTransactionRepository checkoutTransactionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("收到订单支付事件，完成结算交易：orderNo={}", event.getOrderNo());

        CheckoutTransaction transaction = checkoutTransactionRepository
                .findByOrderNo(event.getOrderNo())
                .orElse(null);

        if (transaction == null) {
            log.warn("CheckoutTransaction 不存在，跳过：orderNo={}", event.getOrderNo());
            return;
        }

        // 幂等处理：如果已经是 SUCCESS，不再重复处理
        if (transaction.getStatus() == CheckoutStatus.SUCCESS) {
            log.info("CheckoutTransaction 已为 SUCCESS 状态，跳过重复处理：orderNo={}", event.getOrderNo());
            return;
        }

        if (transaction.getStatus() != CheckoutStatus.PROCESSING) {
            log.warn("CheckoutTransaction 状态为 {}，不能执行 success()：orderNo={}",
                    transaction.getStatus(), event.getOrderNo());
            return;
        }

        // 完成结算交易
        transaction.success(event.getOrderNo());
        checkoutTransactionRepository.save(transaction);

        log.info("结算交易完成：checkoutNo={}, orderNo={}, status=SUCCESS",
                transaction.getCheckoutNo(), event.getOrderNo());
    }
}