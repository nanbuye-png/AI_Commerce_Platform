package com.commerce.platform.ai.controller;

import com.commerce.platform.ai.dto.InternalAddCartItemRequest;
import com.commerce.platform.cart.dto.response.CartVO;
import com.commerce.platform.cart.service.CartApplicationService;
import com.commerce.platform.common.entity.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/ai/cart")
public class InternalAiCartController {

    private final CartApplicationService cartApplicationService;

    @PostMapping("/items")
    public Result<CartVO> addItem(@Valid @RequestBody InternalAddCartItemRequest request) {
        return Result.success(cartApplicationService.addItemBySku(
                request.userId(), request.skuId(), request.quantity()));
    }
}
