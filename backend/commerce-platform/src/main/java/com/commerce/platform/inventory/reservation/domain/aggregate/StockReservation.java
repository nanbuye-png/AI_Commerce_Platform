package com.commerce.platform.inventory.reservation.domain.aggregate;

import com.commerce.platform.inventory.reservation.domain.exception.InvalidReservationStatusException;
import com.commerce.platform.inventory.reservation.domain.valueobject.ReservationStatus;

import java.time.LocalDateTime;

/**
 * 库存预占聚合根
 * <p>
 * 表示一次库存锁定的完整生命周期，聚合根负责维护自身状态。
 * 所有状态变更必须通过领域方法完成，禁止外部直接修改字段。
 * </p>
 *
 * <pre>
 * 状态流：
 * RESERVED
 *   ↓
 * CONFIRMED（支付成功，正式占用）
 *
 * RESERVED
 *   ↓
 * RELEASED（订单取消，释放库存）
 *
 * RESERVED
 *   ↓
 * FAILED（预占失败）
 * </pre>
 */
public class StockReservation {

    private Long id;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime releasedAt;

    /**
     * 创建新的库存预占
     *
     * @param orderId   订单ID
     * @param productId 商品ID
     * @param quantity  数量
     * @return 新建的库存预占（状态为 RESERVED）
     */
    public static StockReservation create(Long orderId, Long productId, Integer quantity) {
        StockReservation reservation = new StockReservation();
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.RESERVED;
        reservation.createdAt = LocalDateTime.now();
        return reservation;
    }

    /**
     * 从持久化恢复库存预占（全字段构造）
     *
     * @param id          预占ID
     * @param orderId     订单ID
     * @param productId   商品ID
     * @param quantity    数量
     * @param status      当前状态
     * @param createdAt   创建时间
     * @param confirmedAt 确认时间
     * @param releasedAt  释放时间
     * @return 恢复的库存预占
     */
    public static StockReservation restore(Long id, Long orderId, Long productId, Integer quantity,
                                           ReservationStatus status, LocalDateTime createdAt,
                                           LocalDateTime confirmedAt, LocalDateTime releasedAt) {
        StockReservation reservation = new StockReservation();
        reservation.id = id;
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.quantity = quantity;
        reservation.status = status;
        reservation.createdAt = createdAt;
        reservation.confirmedAt = confirmedAt;
        reservation.releasedAt = releasedAt;
        return reservation;
    }

    // ============================================
    // 领域行为 —— 状态流转（由 Aggregate 自身维护）
    // ============================================

    /**
     * 确认预占
     * <p>
     * 支付成功，库存正式占用。
     * RESERVED → CONFIRMED
     * </p>
     */
    public void confirm() {
        transitionTo(ReservationStatus.CONFIRMED, "confirm");
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * 释放预占
     * <p>
     * 订单取消，释放库存。
     * RESERVED → RELEASED
     * </p>
     */
    public void release() {
        transitionTo(ReservationStatus.RELEASED, "release");
        this.releasedAt = LocalDateTime.now();
    }

    /**
     * 标记预占失败
     * <p>
     * RESERVED → FAILED
     * </p>
     */
    public void fail() {
        transitionTo(ReservationStatus.FAILED, "fail");
    }

    // ============================================
    // 内部状态维护
    // ============================================

    /**
     * 执行状态迁移
     *
     * @param target    目标状态
     * @param operation 操作名称
     * @throws InvalidReservationStatusException 如果迁移非法
     */
    private void transitionTo(ReservationStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidReservationStatusException(
                    this.id, this.status.name(), target.name(), operation);
        }
        this.status = target;
    }

    /**
     * 设置聚合根ID（仅用于持久化后的赋值）
     *
     * @param id 预占ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    // ============================================
    // Getters
    // ============================================

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }
}