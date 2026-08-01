package com.commerce.platform.cart.service;

import com.commerce.platform.cart.domain.entity.Cart;
import com.commerce.platform.cart.domain.entity.CartItem;
import com.commerce.platform.cart.domain.repository.CartItemRepository;
import com.commerce.platform.cart.domain.repository.CartRepository;
import com.commerce.platform.cart.dto.request.AddCartItemRequest;
import com.commerce.platform.cart.dto.request.RemoveCartItemRequest;
import com.commerce.platform.cart.dto.request.UpdateCartItemRequest;
import com.commerce.platform.cart.dto.response.CartItemVO;
import com.commerce.platform.cart.dto.response.CartVO;
import com.commerce.platform.cart.exception.CartNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartApplicationService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public CartVO getCart(Long userId) {
        Cart cart = findOrCreateCart(userId);
        return toCartVO(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public CartVO addItem(Long userId, AddCartItemRequest request) {
        Cart cart = findOrCreateCart(userId);
        cart.addItem(request.getProductId(), request.getSkuId(),
                request.getProductName(), request.getProductImage(),
                request.getPrice(), request.getQuantity());
        cartRepository.save(cart);
        log.info("购物车添加商品：userId={}, skuId={}, quantity={}",
                userId, request.getSkuId(), request.getQuantity());
        return toCartVO(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public CartVO updateQuantity(Long userId, UpdateCartItemRequest request) {
        Cart cart = findCartByUserId(userId);
        cart.updateQuantity(request.getSkuId(), request.getQuantity());
        cartRepository.save(cart);
        log.info("购物车修改数量：userId={}, skuId={}, quantity={}",
                userId, request.getSkuId(), request.getQuantity());
        return toCartVO(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public CartVO removeItem(Long userId, RemoveCartItemRequest request) {
        Cart cart = findCartByUserId(userId);
        // 直接对数据库执行 bulk DELETE 物理删除指定 SKU 条目。
        // @Modifying(clearAutomatically=true) 会清空持久化上下文，
        // 避免 Hibernate 对 Cart 的 @OneToMany orphanRemoval 集合做 diff 时
        // 执行 "UPDATE cart_item SET cart_id=null"（cart_id 为 NOT NULL 违反约束）。
        cartItemRepository.deleteByCartIdAndSkuId(cart.getId(), request.getSkuId());
        log.info("购物车删除商品：userId={}, skuId={}", userId, request.getSkuId());

        // 删除后重新加载购物车，返回最新状态
        Cart refreshed = cartRepository.findByUserId(userId)
                .orElse(cart);
        return toCartVO(refreshed);
    }

    private Cart findCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    private Cart findOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });
    }

    private CartVO toCartVO(Cart cart) {
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setUserId(cart.getUserId());
        List<CartItemVO> itemVOs = cart.getActiveItems().stream()
                .map(this::toCartItemVO)
                .toList();
        vo.setItems(itemVOs);
        return vo;
    }

    private CartItemVO toCartItemVO(CartItem item) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setSkuId(item.getSkuId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setProductImage(item.getProductImage());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSelected(item.getSelected());
        return vo;
    }
}