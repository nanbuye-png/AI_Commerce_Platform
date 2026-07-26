package com.commerce.platform.inventory.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.inventory.dto.merchant.*;
import com.commerce.platform.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 商家端库存管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/inventory")
@RequiredArgsConstructor
public class MerchantInventoryController {

    private final InventoryService inventoryService;

    /**
     * 查询我的库存列表
     */
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Page<InventoryListResponse>> listInventory(Authentication authentication,
                                                              InventoryQueryRequest query) {
        Long merchantId = getMerchantId(authentication);
        Page<InventoryListResponse> page = inventoryService.listInventory(merchantId, query);
        return Result.success(page);
    }

    /**
     * 查询库存详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<InventoryDetailResponse> getInventoryDetail(Authentication authentication,
                                                               @PathVariable Long id) {
        Long merchantId = getMerchantId(authentication);
        InventoryDetailResponse response = inventoryService.getInventoryDetail(merchantId, id);
        return Result.success(response);
    }

    /**
     * 调整库存（增加/减少）
     */
    @PutMapping("/{id}/adjust")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> adjustInventory(Authentication authentication,
                                         @PathVariable Long id,
                                         @Valid @RequestBody InventoryAdjustRequest request) {
        Long merchantId = getMerchantId(authentication);
        inventoryService.adjustInventory(merchantId, id, request);
        return Result.success();
    }

    /**
     * 入库
     */
    @PostMapping("/{id}/inbound")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> inboundInventory(Authentication authentication,
                                          @PathVariable Long id,
                                          @Valid @RequestBody InventoryAdjustRequest request) {
        Long merchantId = getMerchantId(authentication);
        inventoryService.inboundInventory(merchantId, id, request);
        return Result.success();
    }

    /**
     * 查询库存流水
     */
    @GetMapping("/{id}/movements")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Page<InventoryMovementResponse>> listMovements(Authentication authentication,
                                                                  @PathVariable Long id,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "20") int pageSize) {
        Long merchantId = getMerchantId(authentication);
        Page<InventoryMovementResponse> movements = inventoryService.listInventoryMovements(merchantId, id, page, pageSize);
        return Result.success(movements);
    }

    /**
     * 从 Authentication 中获取商家 ID
     */
    private Long getMerchantId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}