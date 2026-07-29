package com.commerce.platform.warehouse.domain.exception;

import com.commerce.platform.common.exception.BusinessException;

/**
 * 仓库状态变更异常
 * <p>
 * 当仓库进行非法状态迁移时抛出。
 * </p>
 */
public class InvalidWarehouseStatusException extends BusinessException {

    public InvalidWarehouseStatusException(Long warehouseId, String currentStatus, String targetStatus) {
        super("仓库状态非法迁移: warehouseId=" + warehouseId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus);
    }
}