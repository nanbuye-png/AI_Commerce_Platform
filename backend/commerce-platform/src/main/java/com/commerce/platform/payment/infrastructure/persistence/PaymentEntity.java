package com.commerce.platform.payment.infrastructure.persistence;

import com.commerce.platform.common.entity.BaseEntity;
import com.commerce.platform.payment.domain.valueobject.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_no", columnList = "payment_no", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity extends BaseEntity {

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "payment_no", nullable = false, unique = true, length = 64)
    private String paymentNo;

    @Column(name = "transaction_no", length = 128)
    private String transactionNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @PrePersist
    protected void onCreate() {
        setCreatedTime(LocalDateTime.now());
        setUpdatedTime(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedTime(LocalDateTime.now());
    }
}