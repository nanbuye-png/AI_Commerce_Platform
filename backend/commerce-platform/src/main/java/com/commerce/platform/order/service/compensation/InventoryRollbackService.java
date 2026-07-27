package com.commerce.platform.order.service.compensation;

/**
 * 库存回滚服务接口（预留）
 * <p>
 * 当订单取消或关闭时，需要释放已锁定的库存。
 * 完整实现将在 Payment Domain 接入时完成。
 * </p>
 */
public interface InventoryRollbackService {

    /**
     * 释放订单占用的库存
     *
     * @param orderNo 订单号
     */
    void releaseInventory(String orderNo);
}