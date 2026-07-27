package com.commerce.platform.cart.domain.entity;

import com.commerce.platform.cart.domain.enums.CheckoutStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 结算交易记录（Saga 事务日志）
 * <p>
 * 记录每次结算的完整生命周期，确保 Checkout Saga 的可追溯性和补偿能力。
 * 禁止外部直接修改 status，必须通过领域方法 start() / success() / fail() 变更状态。
 * </p>
 */
@Entity
@Table(name = "checkout_transaction", indexes = {
    @Index(name = "idx_checkout_no", columnList = "checkout_no", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_order_no", columnList = "order_no")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 结算单号 */
    @Column(name = "checkout_no", nullable = false, unique = true, length = 64)
    private String checkoutNo;

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 购物车ID */
    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    /** 结算状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.PRIVATE)
    private CheckoutStatus status;

    /** 关联订单号（成功时赋值） */
    @Column(name = "order_no", length = 64)
    private String orderNo;

    /** 失败原因（失败时赋值） */
    @Column(name = "fail_reason", length = 500)
    private String failReason;

    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    // ============================================
    // 领域行为 —— 结算状态流转
    // 禁止外部直接修改 status
    // ============================================

    /**
     * 开始结算
     * <p>
     * INIT → PROCESSING
     * </p>
     */
    public void start() {
        if (this.status != CheckoutStatus.INIT) {
            throw new IllegalStateException("结算已开始，状态为 " + this.status + "，不能重复 start()");
        }
        this.status = CheckoutStatus.PROCESSING;
    }

    /**
     * 结算成功
     * <p>
     * PROCESSING → SUCCESS
     * </p>
     *
     * @param orderNo 关联的订单号
     */
    public void success(String orderNo) {
        if (this.status != CheckoutStatus.PROCESSING) {
            throw new IllegalStateException("结算状态为 " + this.status + "，不能执行 success()");
        }
        this.status = CheckoutStatus.SUCCESS;
        this.orderNo = orderNo;
    }

    /**
     * 结算失败
     * <p>
     * PROCESSING → FAILED
     * </p>
     *
     * @param reason 失败原因
     */
    public void fail(String reason) {
        if (this.status != CheckoutStatus.PROCESSING && this.status != CheckoutStatus.INIT) {
            throw new IllegalStateException("结算状态为 " + this.status + "，不能执行 fail()");
        }
        this.status = CheckoutStatus.FAILED;
        this.failReason = reason;
    }
}