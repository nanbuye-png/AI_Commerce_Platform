package com.commerce.platform.cart.domain.entity;

import com.commerce.platform.cart.domain.enums.CartItemStatus;
import com.commerce.platform.cart.exception.CartItemNotFoundException;
import com.commerce.platform.cart.exception.InvalidCartOperationException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "cart", indexes = {
    @Index(name = "idx_cart_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

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
    // 领域行为 —— 购物车操作
    // 禁止外部直接 getItems().add()
    // ============================================

    public void addItem(Long productId, Long skuId, String productName,
                        String productImage, BigDecimal price, Integer quantity) {
        if (quantity <= 0) {
            throw new InvalidCartOperationException("商品数量必须大于0");
        }

        Optional<CartItem> existing = items.stream()
                .filter(item -> item.getSkuId().equals(skuId) && item.getStatus() == CartItemStatus.ACTIVE)
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.updateQuantity(item.getQuantity() + quantity);
        } else {
            CartItem item = CartItem.builder()
                    .cartId(this.id)
                    .productId(productId)
                    .skuId(skuId)
                    .productName(productName)
                    .productImage(productImage)
                    .price(price)
                    .quantity(quantity)
                    .selected(true)
                    .status(CartItemStatus.ACTIVE)
                    .build();
            items.add(item);
        }
    }

    public void updateQuantity(Long skuId, Integer quantity) {
        if (quantity <= 0) {
            throw new InvalidCartOperationException("商品数量必须大于0");
        }
        CartItem item = findActiveItem(skuId);
        item.updateQuantity(quantity);
    }

    public void removeItem(Long skuId) {
        CartItem item = findActiveItem(skuId);
        item.remove();
    }

    public List<CartItem> getActiveItems() {
        return items.stream()
                .filter(item -> item.getStatus() == CartItemStatus.ACTIVE)
                .toList();
    }

    private CartItem findActiveItem(Long skuId) {
        return items.stream()
                .filter(item -> item.getSkuId().equals(skuId) && item.getStatus() == CartItemStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(skuId));
    }
}