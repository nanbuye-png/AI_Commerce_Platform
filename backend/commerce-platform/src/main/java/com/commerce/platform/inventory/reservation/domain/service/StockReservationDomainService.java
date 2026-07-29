package com.commerce.platform.inventory.reservation.domain.service;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.reservation.domain.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 库存预占领域服务
 * <p>
 * 负责创建库存预占的业务协调，例如校验库存数量。
 * 不承担状态机职责，状态机在 Aggregate 内维护。
 * 不直接保存数据库，持久化由 Application Handler 协调。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class StockReservationDomainService {

    private final StockReservationRepository stockReservationRepository;

    /**
     * 创建库存预占
     * <p>
     * 创建新的库存预占记录。
     * </p>
     *
     * @param orderId   订单ID
     * @param productId 商品ID
     * @param quantity  预占数量
     * @return 新建的库存预占
     */
    public StockReservation createReservation(Long orderId, Long productId, Integer quantity) {
        // 校验数量
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("预占数量必须大于0: quantity=" + quantity);
        }

        // 创建库存预占（返回 RESERVED 状态的聚合）
        return StockReservation.create(orderId, productId, quantity);
    }
}