package com.commerce.platform.shipping.domain.valueobject;

/**
 * 配送收货状态枚举
 * <p>
 * 与 ShipmentStatus 分离，描述末端配送/签收维度。
 * 物流状态和收货状态属于不同维度。
 * </p>
 */
public enum DeliveryStatus {
    WAITING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED
}