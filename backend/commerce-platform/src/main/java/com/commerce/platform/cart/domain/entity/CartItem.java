package com.commerce.platform.cart.domain.entity;

import com.commerce.platform.cart.domain.enums.CartItemStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cart_sku", columnNames = {"cart_id", "sku_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cart_id", nullable = false, updatable = false)
    private Long cartId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "sku_id", nullable = false, updatable = false)
    private Long skuId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "product_image", length = 500)
    private String productImage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean selected = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CartItemStatus status = CartItemStatus.ACTIVE;

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

    // === 领域行为 ===

    public void remove() {
        this.status = CartItemStatus.REMOVED;
    }

    public void checkout() {
        this.status = CartItemStatus.CHECKED_OUT;
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 恢复商品为 ACTIVE 状态（补偿操作）
     * <p>
     * 当结算失败时，将 CHECKED_OUT 的商品恢复为 ACTIVE。
     * </p>
     */
    public void restore() {
        if (this.status != CartItemStatus.CHECKED_OUT) {
            throw new IllegalStateException("商品状态为 " + this.status + "，不能执行 restore()");
        }
        this.status = CartItemStatus.ACTIVE;
    }
}