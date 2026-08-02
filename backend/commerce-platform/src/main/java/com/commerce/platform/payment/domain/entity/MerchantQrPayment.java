package com.commerce.platform.payment.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户二维码收款流水实体
 */
@Entity
@Table(name = "merchant_qr_payment", indexes = {
    @Index(name = "idx_mqp_order_no", columnList = "order_no"),
    @Index(name = "idx_mqp_qr_token", columnList = "qr_token"),
    @Index(name = "idx_mqp_status_expire", columnList = "status, expire_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class MerchantQrPayment extends BaseEntity {

    @Column(name = "payment_no", nullable = false, unique = true, length = 32, updatable = false)
    private String paymentNo;

    @Column(name = "order_no", nullable = false, length = 32, updatable = false)
    private String orderNo;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private Long buyerId;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private Long merchantId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "qr_token", nullable = false, length = 64)
    private String qrToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MerchantQrPaymentStatus status = MerchantQrPaymentStatus.WAITING;

    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;

    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    public void markPaid() {
        if (this.status != MerchantQrPaymentStatus.WAITING) {
            throw new IllegalStateException("支付流水不是待支付状态，无法支付：paymentNo=" + paymentNo);
        }
        if (LocalDateTime.now().isAfter(expireTime)) {
            this.status = MerchantQrPaymentStatus.EXPIRED;
            throw new IllegalStateException("支付二维码已过期：paymentNo=" + paymentNo);
        }
        this.status = MerchantQrPaymentStatus.PAID;
        this.paidTime = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != MerchantQrPaymentStatus.WAITING) {
            throw new IllegalStateException("支付流水不是待支付状态，无法取消：paymentNo=" + paymentNo);
        }
        this.status = MerchantQrPaymentStatus.CANCELLED;
        this.cancelledTime = LocalDateTime.now();
    }

    public void markExpired() {
        this.status = MerchantQrPaymentStatus.EXPIRED;
    }
}

