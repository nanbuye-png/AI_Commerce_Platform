package com.commerce.platform.inventory.service;

import com.commerce.platform.inventory.dto.reservation.*;
import org.springframework.data.domain.Page;

/**
 * 库存预占服务接口
 * <p>
 * 提供库存锁定、释放、扣减等操作。
 * 所有操作必须经过此 Service，禁止 Controller 直接操作 Repository。
 * </p>
 */
public interface InventoryReservationService {

    /**
     * 锁定库存（订单创建时调用）
     * <p>
     * 流程：
     * 1. 校验可用库存 >= 锁定数量
     * 2. availableStock -= quantity, reservedStock += quantity
     * 3. 创建 InventoryReservation（status=ACTIVE）
     * 4. 生成 InventoryMovement（RESERVE）
     * 5. 发布 InventoryReservedEvent
     * </p>
     *
     * @param request 锁定请求
     * @return 预占响应
     */
    ReservationResponse reserve(ReserveInventoryRequest request);

    /**
     * 释放库存（订单取消时调用）
     * <p>
     * 流程：
     * 1. 校验 Reservation 状态为 ACTIVE
     * 2. reservedStock -= quantity, availableStock += quantity
     * 3. 更新 Reservation 状态为 RELEASED
     * 4. 生成 InventoryMovement（RELEASE）
     * 5. 发布 InventoryReleasedEvent
     * </p>
     *
     * @param request 释放请求
     * @return 预占响应
     */
    ReservationResponse release(ReleaseReservationRequest request);

    /**
     * 扣减库存（支付成功时调用）
     * <p>
     * 流程：
     * 1. 校验 Reservation 状态为 ACTIVE
     * 2. reservedStock -= quantity, totalStock -= quantity
     * 3. 更新 Reservation 状态为 DEDUCTED
     * 4. 生成 InventoryMovement（DEDUCT）
     * 5. 发布 InventoryDeductedEvent
     * </p>
     *
     * @param request 扣减请求
     * @return 预占响应
     */
    ReservationResponse deduct(DeductReservationRequest request);

    /**
     * 查询预占详情
     *
     * @param reservationNo 预占编号
     * @return 预占详情
     */
    ReservationDetailResponse getReservation(String reservationNo);

    /**
     * 分页查询预占列表
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return 预占列表
     */
    Page<ReservationDetailResponse> listReservations(int page, int pageSize);
}