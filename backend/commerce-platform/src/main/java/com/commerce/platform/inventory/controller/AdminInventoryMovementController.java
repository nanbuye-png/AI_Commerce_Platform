package com.commerce.platform.inventory.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.inventory.dto.movement.InventoryMovementDetailResponse;
import com.commerce.platform.inventory.dto.movement.InventoryMovementQueryRequest;
import com.commerce.platform.inventory.dto.movement.InventoryMovementResponse;
import com.commerce.platform.inventory.service.InventoryMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端库存流水查询 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/inventory/movements")
@RequiredArgsConstructor
public class AdminInventoryMovementController {

    private final InventoryMovementService movementService;

    /**
     * 查询全平台库存流水
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<InventoryMovementResponse>> listAllMovements(InventoryMovementQueryRequest query) {
        Page<InventoryMovementResponse> page = movementService.listAllMovements(query);
        return Result.success(page);
    }

    /**
     * 查询流水详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<InventoryMovementDetailResponse> getMovementDetail(@PathVariable Long id) {
        InventoryMovementDetailResponse detail = movementService.getMovementDetail(id);
        return Result.success(detail);
    }
}