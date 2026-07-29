package com.commerce.platform.returns.event.listener;

import com.commerce.platform.refund.application.command.CreateRefundCommand;
import com.commerce.platform.refund.application.handler.CreateRefundHandler;
import com.commerce.platform.refund.domain.valueobject.RefundReason;
import com.commerce.platform.returns.domain.event.ReturnCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

/**
 * 退货完成事件监听器
 * <p>
 * 监听 ReturnCompletedEvent，触发退款流程。
 * Return 不直接调用 Refund，通过 Event 驱动。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnCompletedForRefundEventListener {

    private final CreateRefundHandler createRefundHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReturnCompleted(ReturnCompletedEvent event) {
        log.info("收到退货完成事件，开始创建退款: returnId={}, orderId={}",
                event.getReturnId(), event.getOrderId());

        try {
            // 创建退款命令（退款金额和原因需要业务层补充）
            CreateRefundCommand command = new CreateRefundCommand(
                    event.getOrderId(), -1L, BigDecimal.ZERO, RefundReason.OTHER);

            createRefundHandler.handle(command);

            log.info("退款已触发: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("退货完成事件处理失败: returnId={}, error={}",
                    event.getReturnId(), e.getMessage(), e);
        }
    }
}