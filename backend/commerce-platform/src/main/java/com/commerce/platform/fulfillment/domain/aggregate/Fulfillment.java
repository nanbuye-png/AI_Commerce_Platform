package com.commerce.platform.fulfillment.domain.aggregate;

import com.commerce.platform.fulfillment.domain.exception.InvalidFulfillmentStatusException;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import com.commerce.platform.fulfillment.domain.valueobject.ShipmentInfo;

import java.time.LocalDateTime;

/**
 * 履约单聚合根
 * <p>
 * 表示一次订单履约的完整生命周期，聚合根负责维护自身状态。
 * 所有状态变更必须通过领域方法完成，禁止外部直接修改字段。
 * </p>
 *
 * <pre>
 * 状态流：PENDING → PROCESSING → PICKING → PACKING → WAITING_SHIPMENT → SHIPPED → DELIVERED → COMPLETED
 * 任意非终态 → CANCELLED / FAILED
 * </pre>
 */
public class Fulfillment {

    /** 履约单ID */
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 商家ID */
    private Long merchantId;

    /** 仓库ID（允许为空） */
    private Long warehouseId;

    /** 履约状态 */
    private FulfillmentStatus status;

    /** 物流信息 */
    private ShipmentInfo shipmentInfo;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /**
     * 创建新的履约单
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     * @return 新建的履约单（状态为 PENDING）
     */
    public static Fulfillment create(Long orderId, Long merchantId) {
        Fulfillment fulfillment = new Fulfillment();
        fulfillment.orderId = orderId;
        fulfillment.merchantId = merchantId;
        fulfillment.status = FulfillmentStatus.PENDING;
        fulfillment.createdAt = LocalDateTime.now();
        fulfillment.updatedAt = LocalDateTime.now();
        return fulfillment;
    }

    /**
     * 从持久化恢复履约单（全字段构造）
     *
     * @param id           履约单ID
     * @param orderId      订单ID
     * @param merchantId   商家ID
     * @param warehouseId  仓库ID
     * @param status       当前状态
     * @param shipmentInfo 物流信息
     * @param createdAt    创建时间
     * @param updatedAt    更新时间
     * @return 恢复的履约单
     */
    public static Fulfillment restore(Long id, Long orderId, Long merchantId, Long warehouseId,
                                      FulfillmentStatus status, ShipmentInfo shipmentInfo,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        Fulfillment fulfillment = new Fulfillment();
        fulfillment.id = id;
        fulfillment.orderId = orderId;
        fulfillment.merchantId = merchantId;
        fulfillment.warehouseId = warehouseId;
        fulfillment.status = status;
        fulfillment.shipmentInfo = shipmentInfo;
        fulfillment.createdAt = createdAt;
        fulfillment.updatedAt = updatedAt;
        return fulfillment;
    }

    // ============================================
    // 领域行为 —— 状态流转（由 Aggregate 自身维护）
    // ============================================

    /**
     * 开始处理履约单
     * <p>
     * PENDING → PROCESSING
     * </p>
     */
    public void startProcessing() {
        transitionTo(FulfillmentStatus.PROCESSING, "startProcessing");
    }

    /**
     * 开始拣货
     * <p>
     * PROCESSING → PICKING
     * </p>
     */
    public void startPicking() {
        transitionTo(FulfillmentStatus.PICKING, "startPicking");
    }

    /**
     * 开始打包
     * <p>
     * PICKING → PACKING
     * </p>
     */
    public void startPacking() {
        transitionTo(FulfillmentStatus.PACKING, "startPacking");
    }

    /**
     * 标记等待发货
     * <p>
     * PACKING → WAITING_SHIPMENT
     * </p>
     */
    public void markWaitingShipment() {
        transitionTo(FulfillmentStatus.WAITING_SHIPMENT, "markWaitingShipment");
    }

    /**
     * 发货（设置物流信息）
     * <p>
     * WAITING_SHIPMENT → SHIPPED
     * </p>
     *
     * @param shipmentInfo 物流信息
     */
    public void ship(ShipmentInfo shipmentInfo) {
        transitionTo(FulfillmentStatus.SHIPPED, "ship");
        this.shipmentInfo = shipmentInfo;
    }

    /**
     * 确认送达
     * <p>
     * SHIPPED → DELIVERED
     * </p>
     */
    public void deliver() {
        transitionTo(FulfillmentStatus.DELIVERED, "deliver");
    }

    /**
     * 完成履约单
     * <p>
     * DELIVERED → COMPLETED
     * </p>
     */
    public void complete() {
        transitionTo(FulfillmentStatus.COMPLETED, "complete");
    }

    /**
     * 取消履约单
     * <p>
     * 非终态 → CANCELLED
     * </p>
     */
    public void cancel() {
        transitionTo(FulfillmentStatus.CANCELLED, "cancel");
    }

    /**
     * 标记履约单失败
     * <p>
     * 非终态 → FAILED
     * </p>
     */
    public void fail() {
        transitionTo(FulfillmentStatus.FAILED, "fail");
    }

    // ============================================
    // 内部状态维护
    // ============================================

    /**
     * 执行状态迁移
     *
     * @param target    目标状态
     * @param operation 操作名称
     * @throws InvalidFulfillmentStatusException 如果迁移非法
     */
    private void transitionTo(FulfillmentStatus target, String operation) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidFulfillmentStatusException(
                    this.id, this.status.name(), target.name(), operation);
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置聚合根ID（仅用于持久化后的赋值）
     *
     * @param id 履约单ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 设置仓库ID
     *
     * @param warehouseId 仓库ID
     */
    public void assignWarehouse(Long warehouseId) {
        this.warehouseId = warehouseId;
        this.updatedAt = LocalDateTime.now();
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

    public Long getMerchantId() {
        return merchantId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public FulfillmentStatus getStatus() {
        return status;
    }

    public ShipmentInfo getShipmentInfo() {
        return shipmentInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}