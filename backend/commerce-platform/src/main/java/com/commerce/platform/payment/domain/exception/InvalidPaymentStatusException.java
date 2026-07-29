package com.commerce.platform.payment.domain.exception;

import com.commerce.platform.common.exception.BusinessException;

/**
 * 支付状态变更异常
 * <p>
 * 当支付交易进行非法状态迁移时抛出。
 * </p>
 */
public class InvalidPaymentStatusException extends BusinessException {

    /**
     * 构造状态变更异常
     *
     * @param paymentId     支付ID
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     */
    public InvalidPaymentStatusException(Long paymentId, String currentStatus, String targetStatus) {
        super("支付状态非法迁移: paymentId=" + paymentId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus);
    }

    /**
     * 构造状态变更异常（含操作描述）
     *
     * @param paymentId     支付ID
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @param operation     操作描述
     */
    public InvalidPaymentStatusException(Long paymentId, String currentStatus, String targetStatus, String operation) {
        super("支付状态非法迁移: paymentId=" + paymentId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus
                + ", operation=" + operation);
    }
}