package com.commerce.platform.cart.service;

import com.commerce.platform.cart.domain.entity.Cart;
import com.commerce.platform.cart.domain.entity.CartItem;
import com.commerce.platform.cart.domain.enums.CartItemStatus;
import com.commerce.platform.cart.domain.repository.CartItemRepository;
import com.commerce.platform.cart.domain.repository.CartRepository;
import com.commerce.platform.cart.dto.request.AddCartItemRequest;
import com.commerce.platform.cart.dto.request.RemoveCartItemRequest;
import com.commerce.platform.cart.dto.request.UpdateCartItemRequest;
import com.commerce.platform.cart.dto.response.CartItemVO;
import com.commerce.platform.cart.dto.response.CartVO;
import com.commerce.platform.cart.exception.CartNotFoundException;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import com.commerce.platform.product.entity.ProductSku;
import com.commerce.platform.product.repository.ProductSkuRepository;
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
    private final InventoryStockRepository inventoryStockRepository;
    private final ProductSkuRepository productSkuRepository;

    @Transactional(readOnly = true)
    public CartVO getCart(Long userId) {
        Cart cart = findOrCreateCart(userId);
        return toCartVO(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public CartVO addItem(Long userId, AddCartItemRequest request) {
        // 数据一致性：SKU 若无库存记录则自动补一条（默认库存 10，与种子数据一致，由商家后续入库/编辑调整）
        if (!inventoryStockRepository.existsBySkuId(request.getSkuId())) {
            productSkuRepository.findById(request.getSkuId()).ifPresent(sku -> {
                InventoryStock stock = InventoryStock.create(
                        sku.getProduct().getId(),
                        sku.getId(),
                        10
                );
                inventoryStockRepository.save(stock);
                log.info("加购时自动补齐库存记录 - skuId={}, productId={}, defaultStock=10", sku.getId(), sku.getProduct().getId());
            });
        }

        Cart cart = findOrCreateCart(userId);

        // 基于数据库的 upsert，避免 Hibernate 集合 merge 触发 uk_cart_sku 唯一约束冲突
        cartItemRepository.findByCartIdAndSkuId(cart.getId(), request.getSkuId())
                .ifPresentOrElse(
                        // 已存在 → 累加数量
                        existing -> {
                            existing.updateQuantity(existing.getQuantity() + request.getQuantity());
                            cartItemRepository.save(existing);
                            log.info("购物车已有商品，累加数量：cartId={}, skuId={}, quantity={}",
                                    cart.getId(), request.getSkuId(), existing.getQuantity());
                        },
                        // 不存在 → 新增
                        () -> {
                            CartItem item = CartItem.builder()
                                    .cartId(cart.getId())
                                    .productId(request.getProductId())
                                    .skuId(request.getSkuId())
                                    .productName(request.getProductName())
                                    .productImage(request.getProductImage())
                                    .price(request.getPrice())
                                    .quantity(request.getQuantity())
                                    .selected(true)
                                    .status(CartItemStatus.ACTIVE)
                                    .build();
                            cartItemRepository.save(item);
                            log.info("购物车新增商品：cartId={}, skuId={}, quantity={}",
                                    cart.getId(), request.getSkuId(), request.getQuantity());
                        }
                );

        Cart refreshed = cartRepository.findByUserId(userId).orElse(cart);
        return toCartVO(refreshed);
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