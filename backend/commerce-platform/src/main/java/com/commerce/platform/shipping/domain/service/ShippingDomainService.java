package com.commerce.platform.shipping.domain.service;

import com.commerce.platform.shipping.domain.aggregate.Shipment;
import com.commerce.platform.shipping.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 配送领域服务
 * <p>
 * 负责创建配送单、校验配送状态。
 * 不直接修改 Shipment 字段，状态变化由 Aggregate 自身维护。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ShippingDomainService {

    private final ShipmentRepository shipmentRepository;

    /**
     * 创建配送单
     *
     * @param fulfillmentId 履约单ID
     * @param packingTaskId 打包任务ID
     * @param carrier       物流承运商
     * @return 创建的配送单
     */
    public Shipment createShipment(Long fulfillmentId, Long packingTaskId, String carrier) {
        // 校验是否已存在配送单
        if (shipmentRepository.findByFulfillmentId(fulfillmentId).isPresent()) {
            throw new IllegalStateException("履约单已有配送单: fulfillmentId=" + fulfillmentId);
        }
        return Shipment.create(fulfillmentId, packingTaskId, carrier);
    }
}