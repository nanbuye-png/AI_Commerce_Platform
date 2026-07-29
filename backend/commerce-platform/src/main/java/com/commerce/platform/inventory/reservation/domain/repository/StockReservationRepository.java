package com.commerce.platform.inventory.reservation.domain.repository;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;

import java.util.Optional;

/**
 * 库存预占仓储接口
 * <p>
 * 定义库存预占聚合的持久化操作接口，属于 Port（出站端口）。
 * 仅定义接口，不实现业务逻辑，实现由 Infrastructure 层完成。
 * </p>
 */
public interface StockReservationRepository {

    /**
     * 保存库存预占
     *
     * @param reservation 库存预占聚合
     * @return 保存后的库存预占（含生成的ID）
     */
    StockReservation save(StockReservation reservation);

    /**
     * 根据ID查询库存预占
     *
     * @param id 预占ID
     * @return 库存预占 Optional
     */
    Optional<StockReservation> findById(Long id);
}