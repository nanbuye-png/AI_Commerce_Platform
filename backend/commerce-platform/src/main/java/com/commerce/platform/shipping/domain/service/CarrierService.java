package com.commerce.platform.shipping.domain.service;

import com.commerce.platform.shipping.domain.aggregate.Shipment;

/**
 * 物流承运商服务接口
 * <p>
 * Domain 层定义的 Carrier 抽象接口。Infrastructure 层实现具体适配器。
 * 第一阶段只定义接口，不实现具体物流 API。
 * </p>
 */
public interface CarrierService {

    /**
     * 创建物流运单
     *
     * @param shipment 配送单
     * @return 运单号
     */
    String createShipment(Shipment shipment);

    /**
     * 查询物流轨迹
     *
     * @param trackingNumber 运单号
     * @return 轨迹信息（简化版返回状态描述）
     */
    String queryTracking(String trackingNumber);
}