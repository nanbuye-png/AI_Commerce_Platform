package com.commerce.platform.inventory.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.inventory.dto.reservation.*;
import com.commerce.platform.inventory.service.InventoryReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 库存预占内部 Controller
 * <p>
 * 属于内部业务 API，暂不开放给 Customer。
 * 后续由 Order Domain 通过 RPC/Feign 调用。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/inventory/reservations")
@RequiredArgsConstructor
public class InventoryReservationController {

    private final InventoryReservationService reservationService;

    /**
     * 锁定库存（订单创建时调用）
     */
    @PostMapping("/reserve")
    public Result<ReservationResponse> reserve(@Valid @RequestBody ReserveInventoryRequest request) {
        ReservationResponse response = reservationService.reserve(request);
        return Result.success(response);
    }

    /**
     * 释放库存（订单取消时调用）
     */
    @PostMapping("/release")
    public Result<ReservationResponse> release(@Valid @RequestBody ReleaseReservationRequest request) {
        ReservationResponse response = reservationService.release(request);
        return Result.success(response);
    }

    /**
     * 扣减库存（支付成功时调用）
     */
    @PostMapping("/deduct")
    public Result<ReservationResponse> deduct(@Valid @RequestBody DeductReservationRequest request) {
        ReservationResponse response = reservationService.deduct(request);
        return Result.success(response);
    }

    /**
     * 查询预占详情
     */
    @GetMapping("/{reservationNo}")
    public Result<ReservationDetailResponse> getReservation(@PathVariable String reservationNo) {
        ReservationDetailResponse response = reservationService.getReservation(reservationNo);
        return Result.success(response);
    }

    /**
     * 分页查询预占列表
     */
    @GetMapping
    public Result<Page<ReservationDetailResponse>> listReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<ReservationDetailResponse> reservations = reservationService.listReservations(page, pageSize);
        return Result.success(reservations);
    }
}