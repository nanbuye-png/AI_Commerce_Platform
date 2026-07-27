package com.commerce.platform.cart.domain.entity;

import com.commerce.platform.cart.domain.enums.CartItemStatus;
import com.commerce.platform.cart.exception.CartItemNotFoundException;
import com.commerce.platform.cart.exception.InvalidCartOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cart Entity 状态转换覆盖测试
 */
class CartStateTest {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = Cart.builder().userId(1L).build();
        cart.setId(1L);
    }

    @Test
    @DisplayName("添加商品：items=1")
    void shouldAddItem() {
        cart.addItem(1L, 1001L, "商品A", "img.jpg", BigDecimal.valueOf(100), 2);
        assertEquals(1, cart.getActiveItems().size());
    }

    @Test
    @DisplayName("重复添加同 SKU：数量累加")
    void shouldAccumulateQuantityForSameSku() {
        cart.addItem(1L, 1001L, "商品A", "img.jpg", BigDecimal.valueOf(100), 2);
        cart.addItem(1L, 1001L, "商品A", "img.jpg", BigDecimal.valueOf(100), 3);
        assertEquals(1, cart.getActiveItems().size());
        assertEquals(5, cart.getActiveItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("修改数量：quantity变化")
    void shouldUpdateQuantity() {
        cart.addItem(1L, 1001L, "商品A", "img.jpg", BigDecimal.valueOf(100), 2);
        cart.updateQuantity(1001L, 5);
        assertEquals(5, cart.getActiveItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("删除商品：ACTIVE → REMOVED")
    void shouldRemoveItem() {
        cart.addItem(1L, 1001L, "商品A", "img.jpg", BigDecimal.valueOf(100), 2);
        cart.removeItem(1001L);
        assertEquals(0, cart.getActiveItems().size());
    }

    @Test
    @DisplayName("数量<=0添加：抛 InvalidCartOperationException")
    void shouldThrowWhenQuantityInvalid() {
        assertThrows(InvalidCartOperationException.class,
                () -> cart.addItem(1L, 1001L, "商品A", "img.jpg", BigDecimal.valueOf(100), 0));
    }

    @Test
    @DisplayName("修改不存在SKU：抛 CartItemNotFoundException")
    void shouldThrowWhenUpdateNonExistentSku() {
        assertThrows(CartItemNotFoundException.class,
                () -> cart.updateQuantity(9999L, 5));
    }

    @Test
    @DisplayName("删除不存在SKU：抛 CartItemNotFoundException")
    void shouldThrowWhenRemoveNonExistentSku() {
        assertThrows(CartItemNotFoundException.class,
                () -> cart.removeItem(9999L));
    }

    @Test
    @DisplayName("REMOVED商品不能再次操作：抛 CartItemNotFoundException")
    void shouldNotFindRemovedItem() {
        cart.addItem(1L, 1001L, "商品A", "img.jpg", BigDecimal.valueOf(100), 2);
        cart.removeItem(1001L);
        assertThrows(CartItemNotFoundException.class,
                () -> cart.updateQuantity(1001L, 5));
    }
}