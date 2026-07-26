package com.commerce.platform.order.domain.entity;

import com.commerce.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 订单收货地址实体
 * 下单时保存收货地址快照，创建后不可修改。
 */
@Entity
@Table(name = "order_addresses", uniqueConstraints = {
    @UniqueConstraint(name = "uk_order_id", columnNames = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Column(nullable = false, length = 64)
    private String receiver;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 32)
    private String province;

    @Column(nullable = false, length = 32)
    private String city;

    @Column(nullable = false, length = 32)
    private String district;

    @Column(name = "detail_address", nullable = false, length = 256)
    private String detailAddress;

    @Column(name = "postal_code", length = 10)
    private String postalCode;
}