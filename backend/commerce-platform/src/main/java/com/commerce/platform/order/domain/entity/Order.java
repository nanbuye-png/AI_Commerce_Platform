package com.commerce.platform.order.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.enums.PaymentStatus;
import com.commerce.platform.order.domain.enums.ShippingStatus;
import com.commerce.platform.order.exception.InvalidOrderStatusException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单实体
 * Order Domain 的聚合根。
 * <p>
 * 状态流转由 Entity 自身维护，DomainService 只负责协调。
 * 不允许外部直接 setOrderStatus()。
 * </p>
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_buyer_id", columnList = "buyer_id"),
    @Index(name = "idx_orders_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_order_status", columnList = "order_status"),
    @Index(name = "idx_orders_created_time", columnList = "created_time"),
    @Index(name = "idx_buyer_status_created", columnList = "buyer_id, order_status, created_time"),
    @Index(name = "idx_merchant_status_created", columnList = "merchant_id, order_status, created_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Order extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true, length = 32, updatable = false)
    private String orderNo;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private Long buyerId;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private Long merchantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING_PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status", nullable = false, length = 20)
    @Builder.Default
    private ShippingStatus shippingStatus = ShippingStatus.UNSHIPPED;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "product_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal productAmount;

    @Column(name = "freight_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal freightAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "pay_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal payAmount;

    @Column(length = 500)
    private String buyerRemark;

    @Column(name = "merchant_remark", length = 500)
    private String merchantRemark;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "shipping_time")
    private LocalDateTime shippingTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderAddress address;

    // ============================================
    // 领域行为 —— 状态流转（由 Entity 自身维护）
    // ============================================

    /**
     * 支付订单
     * PENDING_PAYMENT → PAID
     */
    public void pay() {
        assertValidStatus(OrderStatus.PENDING_PAYMENT, "支付");
        this.orderStatus = OrderStatus.PAID;
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentTime = LocalDateTime.now();
    }

    /**
     * 发货（商家操作）
     * PAID → PROCESSING → SHIPPED
     */
    public void ship() {
        if (this.orderStatus != OrderStatus.PAID && this.orderStatus != OrderStatus.PROCESSING) {
            throw new InvalidOrderStatusException(this.orderNo, this.orderStatus.name(), "发货");
        }
        if (this.orderStatus == OrderStatus.PAID) {
            this.orderStatus = OrderStatus.PROCESSING;
        }
        this.orderStatus = OrderStatus.SHIPPED;
        this.shippingStatus = ShippingStatus.SHIPPED;
        this.shippingTime = LocalDateTime.now();
    }

    /**
     * 确认收货
     * SHIPPED → COMPLETED
     */
    public void complete() {
        assertValidStatus(OrderStatus.SHIPPED, "完成");
        this.orderStatus = OrderStatus.COMPLETED;
        this.shippingStatus = ShippingStatus.RECEIVED;
        this.completedTime = LocalDateTime.now();
    }

    /**
     * 取消订单（买家或管理员）
     * PENDING_PAYMENT, PAID → CANCELLED
     */
    public void cancel() {
        if (this.orderStatus != OrderStatus.PENDING_PAYMENT && this.orderStatus != OrderStatus.PAID) {
            throw new InvalidOrderStatusException(this.orderNo, this.orderStatus.name(), "取消");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledTime = LocalDateTime.now();
    }

    /**
     * 关闭订单（管理员对已取消订单操作）
     * CANCELLED → CLOSED
     */
    public void close() {
        assertValidStatus(OrderStatus.CANCELLED, "关闭");
        this.orderStatus = OrderStatus.CLOSED;
    }

    /**
     * 申请退款
     * PAID, PROCESSING, SHIPPED, COMPLETED → REFUNDING
     */
    public void requestRefund() {
        if (this.orderStatus != OrderStatus.PAID && this.orderStatus != OrderStatus.PROCESSING
                && this.orderStatus != OrderStatus.SHIPPED && this.orderStatus != OrderStatus.COMPLETED) {
            throw new InvalidOrderStatusException(this.orderNo, this.orderStatus.name(), "退款");
        }
        this.orderStatus = OrderStatus.REFUNDING;
        this.paymentStatus = PaymentStatus.REFUNDING;
    }

    /**
     * 退款完成
     * REFUNDING → REFUNDED
     */
    public void completeRefund() {
        assertValidStatus(OrderStatus.REFUNDING, "退款完成");
        this.orderStatus = OrderStatus.REFUNDED;
        this.paymentStatus = PaymentStatus.REFUNDED;
    }

    /**
     * 添加订单条目
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * 设置收货地址
     */
    public void setAddress(OrderAddress address) {
        this.address = address;
        if (address != null) {
            address.setOrder(this);
        }
    }

    // ============================================
    // 辅助方法
    // ============================================

    /**
     * 校验当前状态是否为期望状态，否则抛出异常
     */
    private void assertValidStatus(OrderStatus expected, String operation) {
        if (this.orderStatus != expected) {
            throw new InvalidOrderStatusException(this.orderNo, this.orderStatus.name(), operation);
        }
    }
}