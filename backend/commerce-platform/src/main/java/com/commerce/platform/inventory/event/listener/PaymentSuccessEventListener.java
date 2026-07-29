package com.commerce.platform.inventory.event.listener;

import com.commerce.platform.inventory.service.InventoryDeductApplicationService;
import com.commerce.platform.payment.domain.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 支付成功事件监听器
 * <p>
 * 监听 Payment Domain 的支付成功事件。
 * 不依赖 Payment Entity，只依赖 Payment Event。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessEventListener {

    private final InventoryDeductApplicationService deductApplicationService;

    /**
     * 支付成功后扣减库存
     * <p>
     * 注意：实际 SKU 明细需通过 Order 查询获取。
     * 当前为预留实现，后续由完整流程补充。
     * </p>
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("收到支付成功事件：paymentId={}, orderId={}", event.getPaymentId(), event.getOrderId());
        // 预留：后续通过 Order 提供的商品明细逐 SKU 扣库存
        log.info("支付成功待扣库存：orderId={}", event.getOrderId());
    }
}