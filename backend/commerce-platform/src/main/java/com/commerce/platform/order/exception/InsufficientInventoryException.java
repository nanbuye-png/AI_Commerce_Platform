package com.commerce.platform.order.exception;

import com.commerce.platform.common.exception.BusinessException;

/**
 * 库存不足异常
 */
public class InsufficientInventoryException extends BusinessException {

    public InsufficientInventoryException(Long skuId, Integer requested, Integer available) {
        super(32003, String.format("库存不足：SKU=%d，请求数量=%d，当前可售库存=%d", skuId, requested, available));
    }
}