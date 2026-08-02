package com.commerce.platform.profile.controller;

import com.commerce.platform.common.entity.PageResult;
import com.commerce.platform.common.entity.Result;
import com.commerce.platform.profile.dto.AddressRequest;
import com.commerce.platform.profile.dto.AddressVO;
import com.commerce.platform.profile.dto.BrowseHistoryVO;
import com.commerce.platform.profile.dto.CouponVO;
import com.commerce.platform.profile.dto.FavoriteToggleRequest;
import com.commerce.platform.profile.dto.FavoriteVO;
import com.commerce.platform.profile.dto.ProfileUpdateRequest;
import com.commerce.platform.profile.dto.UserProfileVO;
import com.commerce.platform.profile.service.ProfileApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端个人中心 Controller
 * <p>
 * 收货地址、优惠券、收藏夹、浏览历史、账号设置
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final ProfileApplicationService profileApplicationService;

    // ==================== 账号 & 个人资料 ====================

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<UserProfileVO> getProfile(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.getProfile(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<UserProfileVO> updateProfile(Authentication authentication,
                                               @Valid @RequestBody ProfileUpdateRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.updateProfile(userId, request));
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> changePassword(Authentication authentication,
                                       @RequestBody ProfileUpdateRequest.ChangePasswordRequest request) {
        Long userId = getUserId(authentication);
        profileApplicationService.changePassword(userId, request);
        return Result.success();
    }

    // ==================== 收货地址 ====================

    @GetMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<List<AddressVO>> listAddresses(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.listAddresses(userId));
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<AddressVO> createAddress(Authentication authentication,
                                           @Valid @RequestBody AddressRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.createAddress(userId, request));
    }

    @PutMapping("/addresses/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<AddressVO> updateAddress(Authentication authentication,
                                           @PathVariable Long id,
                                           @Valid @RequestBody AddressRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.updateAddress(userId, id, request));
    }

    @DeleteMapping("/addresses/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> deleteAddress(Authentication authentication,
                                      @PathVariable Long id) {
        Long userId = getUserId(authentication);
        profileApplicationService.deleteAddress(userId, id);
        return Result.success();
    }

    @PutMapping("/addresses/{id}/default")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> setDefaultAddress(Authentication authentication,
                                          @PathVariable Long id) {
        Long userId = getUserId(authentication);
        profileApplicationService.setDefaultAddress(userId, id);
        return Result.success();
    }

    // ==================== 优惠券 ====================

    @GetMapping("/coupons")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<List<CouponVO>> listCoupons(Authentication authentication,
                                               @RequestParam(required = false) String status) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.listCoupons(userId, status));
    }

    // ==================== 收藏夹 ====================

    @GetMapping("/favorites")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<PageResult<FavoriteVO>> listFavorites(Authentication authentication,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(authentication);
        Page<FavoriteVO> result = profileApplicationService.listFavorites(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    @PostMapping("/favorites")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> addFavorite(Authentication authentication,
                                    @Valid @RequestBody FavoriteToggleRequest request) {
        Long userId = getUserId(authentication);
        profileApplicationService.addFavorite(userId, request);
        return Result.success();
    }

    @DeleteMapping("/favorites/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> removeFavorite(Authentication authentication,
                                       @PathVariable Long productId) {
        Long userId = getUserId(authentication);
        profileApplicationService.removeFavorite(userId, productId);
        return Result.success();
    }

    // ==================== 浏览历史 ====================

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<List<BrowseHistoryVO>> listBrowseHistory(Authentication authentication,
                                                            @RequestParam(defaultValue = "20") int limit) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.listBrowseHistory(userId, limit));
    }

    @PostMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> addBrowseHistory(Authentication authentication,
                                         @Valid @RequestBody FavoriteToggleRequest request) {
        Long userId = getUserId(authentication);
        profileApplicationService.addBrowseHistory(userId, request);
        return Result.success();
    }

    @DeleteMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> clearBrowseHistory(Authentication authentication) {
        Long userId = getUserId(authentication);
        profileApplicationService.clearBrowseHistory(userId);
        return Result.success();
    }

    // ==================== 库存校验 ====================

    @GetMapping("/stock/{skuId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Integer> getStock(Authentication authentication, @PathVariable Long skuId) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.getStock(skuId));
    }

    @GetMapping("/stock/check")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Boolean> checkStock(Authentication authentication,
                                      @RequestParam Long skuId,
                                      @RequestParam Integer quantity) {
        Long userId = getUserId(authentication);
        return Result.success(profileApplicationService.checkStock(skuId, quantity));
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return 0L;
        }
        return (Long) authentication.getPrincipal();
    }
}