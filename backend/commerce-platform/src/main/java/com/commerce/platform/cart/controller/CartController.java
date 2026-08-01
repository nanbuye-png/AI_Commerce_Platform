package com.commerce.platform.cart.controller;

import com.commerce.platform.cart.dto.request.AddCartItemRequest;
import com.commerce.platform.cart.dto.request.CheckoutRequest;
import com.commerce.platform.cart.dto.request.RemoveCartItemRequest;
import com.commerce.platform.cart.dto.request.UpdateCartItemRequest;
import com.commerce.platform.cart.dto.response.CartVO;
import com.commerce.platform.cart.service.CartApplicationService;
import com.commerce.platform.cart.service.CheckoutApplicationService;
import com.commerce.platform.common.entity.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartApplicationService cartApplicationService;
    private final CheckoutApplicationService checkoutApplicationService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<CartVO> getCart(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(cartApplicationService.getCart(userId));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<CartVO> addItem(Authentication authentication,
                                   @Valid @RequestBody AddCartItemRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(cartApplicationService.addItem(userId, request));
    }

    @PutMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<CartVO> updateQuantity(Authentication authentication,
                                          @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(cartApplicationService.updateQuantity(userId, request));
    }

    @DeleteMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<CartVO> removeItem(Authentication authentication,
                                      @Valid @RequestBody RemoveCartItemRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(cartApplicationService.removeItem(userId, request));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> checkout(Authentication authentication,
                                    @Valid @RequestBody CheckoutRequest request) {
        Long userId = getUserId(authentication);
        String checkoutNo = checkoutApplicationService.checkout(userId, request);
        return Result.success(checkoutNo);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}