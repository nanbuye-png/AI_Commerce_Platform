package com.commerce.platform.inventory.stock.domain.service;

import com.commerce.platform.inventory.reservation.domain.aggregate.StockReservation;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.exception.InsufficientStockException;
import com.commerce.platform.inventory.stock.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 库存预占领域服务
 * <p>
 * 协调 InventoryStock 和 StockReservation 两个 Aggregate。
 * 不直接修改 Aggregate 状态，通过 Aggregate 的领域方法执行变更。
 * 不直接操作数据库，通过 Repository。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryRepository inventoryRepository;

    /**
     * 预占库存
     * <p>
     * 流程：
     * 1. 查询 InventoryStock
     * 2. 检查库存（无异常则成功，否则抛 InsufficientStockException）
     * 3. 执行 inventory.reserve()
     * 4. 保存 Inventory
     * 5. 创建 StockReservation
     * </p>
     *
     * @param inventory 库存聚合
     * @param quantity  预占数量
     * @param orderId   订单ID
     * @return 新建的库存预占
     * @throws InsufficientStockException 库存不足
     */
    public StockReservation reserveStock(InventoryStock inventory, Integer quantity, Long orderId) {
        // 1. 执行库存预占（inventory.reserve 含库存校验）
        inventory.reserve(quantity);

        // 2. 保存 Inventory（由 Application Handler 通过 Repository 保存）
        // Domain Service 只执行领域逻辑，不直接操作数据库

        // 3. 创建 StockReservation
        return StockReservation.create(orderId, inventory.getProductId(), quantity);
    }
}