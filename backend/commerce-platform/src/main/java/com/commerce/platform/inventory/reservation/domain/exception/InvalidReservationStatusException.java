package com.commerce.platform.inventory.reservation.domain.exception;

import com.commerce.platform.common.exception.BusinessException;

/**
 * 库存预占状态变更异常
 * <p>
 * 当库存预占进行非法状态迁移时抛出。
 * </p>
 */
public class InvalidReservationStatusException extends BusinessException {

    /**
     * 构造状态变更异常
     *
     * @param reservationId 预占ID
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     */
    public InvalidReservationStatusException(Long reservationId, String currentStatus, String targetStatus) {
        super("库存预占状态非法迁移: reservationId=" + reservationId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus);
    }

    /**
     * 构造状态变更异常（含操作描述）
     *
     * @param reservationId 预占ID
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @param operation     操作描述
     */
    public InvalidReservationStatusException(Long reservationId, String currentStatus, String targetStatus, String operation) {
        super("库存预占状态非法迁移: reservationId=" + reservationId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus
                + ", operation=" + operation);
    }
}