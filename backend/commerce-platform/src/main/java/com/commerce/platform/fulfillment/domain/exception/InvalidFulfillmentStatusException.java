package com.commerce.platform.fulfillment.domain.exception;

import com.commerce.platform.common.exception.BusinessException;

/**
 * 履约单状态变更异常
 * <p>
 * 当履约单进行非法状态迁移时抛出。
 * </p>
 */
public class InvalidFulfillmentStatusException extends BusinessException {

    /**
     * 构造状态变更异常
     *
     * @param fulfillmentId 履约单ID
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     */
    public InvalidFulfillmentStatusException(Long fulfillmentId, String currentStatus, String targetStatus) {
        super("履约单状态非法迁移: fulfillmentId=" + fulfillmentId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus);
    }

    /**
     * 构造状态变更异常（含操作描述）
     *
     * @param fulfillmentId 履约单ID
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @param operation     操作描述
     */
    public InvalidFulfillmentStatusException(Long fulfillmentId, String currentStatus, String targetStatus, String operation) {
        super("履约单状态非法迁移: fulfillmentId=" + fulfillmentId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus
                + ", operation=" + operation);
    }
}