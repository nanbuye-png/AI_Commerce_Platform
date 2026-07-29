package com.commerce.platform.shipping.domain.exception;

import com.commerce.platform.common.exception.BusinessException;

public class InvalidShipmentStatusException extends BusinessException {
    public InvalidShipmentStatusException(Long shipmentId, String current, String target) {
        super("配送单状态非法迁移: shipmentId=" + shipmentId + ", current=" + current + ", target=" + target);
    }
}