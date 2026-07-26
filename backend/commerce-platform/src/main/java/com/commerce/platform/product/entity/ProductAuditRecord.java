package com.commerce.platform.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 商品审核记录实体（预留）
 * 记录每次审核操作的审计信息
 */
@Entity
@Table(name = "product_audit_record", indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_audit_created", columnList = "created_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(name = "before_status", nullable = false, length = 20)
    private String beforeStatus;

    @Column(name = "after_status", nullable = false, length = 20)
    private String afterStatus;

    @Column(name = "audit_remark", length = 500)
    private String auditRemark;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
    }
}