package com.commerce.platform.inventory.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.inventory.dto.request.CreateInventoryRequest;
import com.commerce.platform.inventory.dto.response.InventoryVO;
import com.commerce.platform.inventory.service.InventoryApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理 Controller
 * <p>
 * 提供库存的创建、查询、锁定、释放接口。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    /**
     * 创建库存
     * <p>
     * 权限：ADMIN / MERCHANT
     * </p>
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<InventoryVO> createInventory(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryVO vo = inventoryApplicationService.createInventory(request);
        return Result.success(vo);
    }

    /**
     * 查询库存
     * <p>
     * 权限：ADMIN / MERCHANT
     * </p>
     */
    @GetMapping("/{skuId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<InventoryVO> getInventory(@PathVariable Long skuId) {
        InventoryVO vo = inventoryApplicationService.getInventory(skuId);
        return Result.success(vo);
    }

    /**
     * 锁定库存
     * <p>
     * 权限：SYSTEM / INTERNAL（暂不开放给 USER）
     * </p>
     */
    @PostMapping("/{skuId}/lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT', 'SYSTEM')")
    public Result<InventoryVO> lockInventory(@PathVariable Long skuId,
                                              @RequestParam Integer quantity,
                                              @RequestParam String orderNo) {
        InventoryVO vo = inventoryApplicationService.lockInventory(skuId, quantity, orderNo);
        return Result.success(vo);
    }

    /**
     * 释放库存
     * <p>
     * 权限：SYSTEM / INTERNAL
     * </p>
     */
    @PostMapping("/{skuId}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT', 'SYSTEM')")
    public Result<InventoryVO> releaseInventory(@PathVariable Long skuId,
                                                 @RequestParam Integer quantity,
                                                 @RequestParam String orderNo) {
        InventoryVO vo = inventoryApplicationService.releaseInventory(skuId, quantity, orderNo);
        return Result.success(vo);
    }
}