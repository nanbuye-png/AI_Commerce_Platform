package com.commerce.platform.cart.service;

import com.commerce.platform.cart.domain.entity.Cart;
import com.commerce.platform.cart.domain.entity.CartItem;
import com.commerce.platform.cart.domain.entity.CheckoutTransaction;
import com.commerce.platform.cart.domain.enums.CartItemStatus;
import com.commerce.platform.cart.domain.enums.CheckoutStatus;
import com.commerce.platform.cart.domain.repository.CartRepository;
import com.commerce.platform.cart.domain.repository.CheckoutTransactionRepository;
import com.commerce.platform.cart.dto.request.CheckoutRequest;
import com.commerce.platform.cart.event.CartCheckedOutEvent;
import com.commerce.platform.cart.exception.CartNotFoundException;
import com.commerce.platform.cart.exception.InvalidCartOperationException;
import com.commerce.platform.cart.dto.response.CartVO;
import com.commerce.platform.cart.dto.response.CartItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 结算 Application Service
 * <p>
 * 负责购物车结算流程：
 * 1. 创建 CheckoutTransaction（INIT）
 * 2. 标记为 PROCESSING
 * 3. 标记购物车商品为 CHECKED_OUT
 * 4. 保存 CheckoutTransaction
 * 5. 发布 CartCheckedOutEvent（携带 checkoutNo）
 * </p>
 *
 * 不依赖任何 Order Domain 的 Repository 或 Entity。
 * 通过 Event 与 Order Domain 解耦。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutApplicationService {

    private final CartRepository cartRepository;
    private final CheckoutTransactionRepository checkoutTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 结算购物车
     *
     * @param userId  用户ID
     * @param request 结算请求
     * @return 结算单号
     */
    @Transactional(rollbackFor = Exception.class)
    public String checkout(Long userId, CheckoutRequest request) {
        // 1. 查找购物车
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        // 2. 获取选中的 ACTIVE 商品
        List<CartItem> checkoutItems = cart.getItems().stream()
                .filter(item -> item.getStatus() == CartItemStatus.ACTIVE
                        && request.getCartItemIds().contains(item.getId()))
                .collect(Collectors.toList());

        if (checkoutItems.isEmpty()) {
            throw new InvalidCartOperationException("未找到可结算的商品，请确认商品已选中且状态为 ACTIVE");
        }

        // 3. 创建 CheckoutTransaction（INIT → PROCESSING）
        String checkoutNo = generateCheckoutNo();
        CheckoutTransaction transaction = CheckoutTransaction.builder()
                .checkoutNo(checkoutNo)
                .userId(userId)
                .cartId(cart.getId())
                .status(CheckoutStatus.INIT)
                .build();
        transaction.start();
        checkoutTransactionRepository.save(transaction);

        // 4. 标记购物车商品为 CHECKED_OUT
        for (CartItem item : checkoutItems) {
            item.checkout();
        }
        cartRepository.save(cart);

        // 5. 发布 CartCheckedOutEvent（携带 checkoutNo）
        List<CartCheckedOutEvent.CartCheckedOutItem> eventItems = checkoutItems.stream()
                .map(item -> new CartCheckedOutEvent.CartCheckedOutItem(
                        item.getSkuId(), item.getProductId(),
                        item.getProductName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toList());
        eventPublisher.publishEvent(new CartCheckedOutEvent(
                checkoutNo, cart.getId(), userId, null, eventItems));

        log.info("结算成功：checkoutNo={}, userId={}, items={}", checkoutNo, userId, checkoutItems.size());

        return checkoutNo;
    }

    private String generateCheckoutNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "CHK" + timestamp + random;
    }
}