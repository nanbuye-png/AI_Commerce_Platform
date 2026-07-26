package com.commerce.platform.inventory.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.inventory.dto.movement.InventoryMovementQueryRequest;
import com.commerce.platform.inventory.dto.movement.InventoryMovementResponse;
import com.commerce.platform.inventory.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 商家端库存流水查询 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/inventory/movements")
@RequiredArgsConstructor
public class MerchantInventoryMovementController {

    private final InventoryMovementService movementService;

    /**
     * 查询本店库存流水
     */
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Page<InventoryMovementResponse>> listMovements(Authentication authentication,
                                                                  InventoryMovementQueryRequest query) {
        Long merchantId = getMerchantId(authentication);
        Page<InventoryMovementResponse> page = movementService.listMovements(merchantId, query);
        return Result.success(page);
    }

    private Long getMerchantId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}