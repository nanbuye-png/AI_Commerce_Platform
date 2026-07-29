package com.commerce.platform.fulfillment.domain.service;

import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.repository.FulfillmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 履约单领域服务
 * <p>
 * 负责履约单创建的业务协调，例如校验是否允许创建履约单。
 * 不承担状态机职责，状态机在 Aggregate 内维护。
 * 不直接保存数据库，持久化由 Application Handler 协调。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FulfillmentDomainService {

    private final FulfillmentRepository fulfillmentRepository;

    /**
     * 创建履约单
     * <p>
     * 校验订单是否已存在履约单，如果不存在则创建新的履约单。
     * </p>
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     * @return 新建的履约单
     * @throws IllegalStateException 如果订单已有履约单
     */
    public Fulfillment createFulfillment(Long orderId, Long merchantId) {
        // 校验是否允许创建：同一订单不允许重复创建履约单
        if (fulfillmentRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("订单已有履约单，不允许重复创建: orderId=" + orderId);
        }

        // 创建履约单（返回 PENDING 状态的聚合）
        return Fulfillment.create(orderId, merchantId);
    }

    /**
     * 校验订单是否可以创建履约单
     *
     * @param orderId 订单ID
     * @return true 如果允许创建
     */
    public boolean canCreateFulfillment(Long orderId) {
        return !fulfillmentRepository.existsByOrderId(orderId);
    }
}