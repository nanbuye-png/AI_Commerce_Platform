package com.commerce.platform.inventory.domain.enums;

/**
 * 库存状态枚举
 * <p>
 * 定义库存的生命周期状态：
 * <ul>
 *   <li>AVAILABLE：可售库存，初始状态</li>
 *   <li>LOCKED：订单占用库存（已锁定）</li>
 *   <li>DEDUCTED：库存已扣减（已出库）</li>
 *   <li>RELEASED：库存已释放（锁定超时/取消释放）</li>
 * </ul>
 * </p>
 *
 * <pre>
 * 状态转换规则：
 * AVAILABLE ──lockStock()──→ LOCKED
 * LOCKED    ──deductStock()──→ DEDUCTED
 * LOCKED    ──releaseStock()──→ RELEASED
 * DEDUCTED  ──restoreStock()──→ AVAILABLE
 * </pre>
 */
public enum InventoryStatus {
    AVAILABLE,
    LOCKED,
    DEDUCTED,
    RELEASED
}