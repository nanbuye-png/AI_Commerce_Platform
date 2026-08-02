package com.commerce.platform.profile.service;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.inventory.stock.domain.aggregate.InventoryStock;
import com.commerce.platform.inventory.stock.domain.repository.InventoryStockRepository;
import com.commerce.platform.profile.dto.*;
import com.commerce.platform.profile.entity.BrowseHistory;
import com.commerce.platform.profile.entity.UserAddress;
import com.commerce.platform.profile.entity.UserCoupon;
import com.commerce.platform.profile.entity.UserFavorite;
import com.commerce.platform.profile.repository.BrowseHistoryRepository;
import com.commerce.platform.profile.repository.UserAddressRepository;
import com.commerce.platform.profile.repository.UserCouponRepository;
import com.commerce.platform.profile.repository.UserFavoriteRepository;
import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * C 端个人中心应用服务
 * <p>
 * 账号设置、收货地址、优惠券、收藏夹、浏览历史、库存校验
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileApplicationService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final BrowseHistoryRepository browseHistoryRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== 账号 & 个人资料 ====================

    @Transactional(readOnly = true)
    public UserProfileVO getProfile(Long userId) {
        User user = findUser(userId);
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUser(userId);
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);
        log.info("更新个人资料 - userId={}", userId);
        return getProfile(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ProfileUpdateRequest.ChangePasswordRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(33001, "原密码不正确");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException(33002, "新密码长度不能少于6位");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("修改密码 - userId={}", userId);
    }

    // ==================== 收货地址 ====================

    @Transactional(readOnly = true)
    public List<AddressVO> listAddresses(Long userId) {
        return userAddressRepository.findByUserIdAndDeletedFalseOrderByIsDefaultDescCreatedTimeDesc(userId)
                .stream()
                .map(this::toAddressVO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressVO createAddress(Long userId, AddressRequest request) {
        boolean isFirst = userAddressRepository.findByUserIdAndDeletedFalseOrderByIsDefaultDescCreatedTimeDesc(userId).isEmpty();
        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault()) || isFirst;

        if (isDefault) {
            clearDefaultAddress(userId);
        }

        UserAddress address = UserAddress.builder()
                .userId(userId)
                .receiver(request.getReceiver())
                .phone(request.getPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detailAddress(request.getDetailAddress())
                .postalCode(request.getPostalCode())
                .isDefault(isDefault)
                .build();
        userAddressRepository.save(address);
        log.info("创建收货地址 - userId={}, addressId={}", userId, address.getId());
        return toAddressVO(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressVO updateAddress(Long userId, Long addressId, AddressRequest request) {
        UserAddress address = findAddress(userId, addressId);

        address.setReceiver(request.getReceiver());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setPostalCode(request.getPostalCode());

        boolean newDefault = Boolean.TRUE.equals(request.getIsDefault());
        if (newDefault && !Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefaultAddress(userId);
            address.setIsDefault(true);
        }
        userAddressRepository.save(address);
        log.info("更新收货地址 - userId={}, addressId={}", userId, addressId);
        return toAddressVO(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = findAddress(userId, addressId);
        address.setDeleted(true);
        userAddressRepository.save(address);
        log.info("删除收货地址 - userId={}, addressId={}", userId, addressId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = findAddress(userId, addressId);
        clearDefaultAddress(userId);
        address.setIsDefault(true);
        userAddressRepository.save(address);
        log.info("设置默认地址 - userId={}, addressId={}", userId, addressId);
    }

    // ==================== 优惠券 ====================

    @Transactional(readOnly = true)
    public List<CouponVO> listCoupons(Long userId, String status) {
        List<UserCoupon> coupons;
        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            coupons = userCouponRepository.findByUserIdAndStatus(userId, status.toUpperCase());
        } else {
            coupons = userCouponRepository.findByUserId(userId);
        }
        LocalDateTime now = LocalDateTime.now();
        return coupons.stream()
                .map(c -> {
                    // 动态更新过期状态
                    if ("UNUSED".equals(c.getStatus()) && c.getExpireTime() != null && c.getExpireTime().isBefore(now)) {
                        c.setStatus("EXPIRED");
                        userCouponRepository.save(c);
                    }
                    return toCouponVO(c);
                })
                .collect(Collectors.toList());
    }

    // ==================== 收藏夹 ====================

    @Transactional(readOnly = true)
    public Page<FavoriteVO> listFavorites(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdTime"));
        return userFavoriteRepository.findByUserId(userId, pageable).map(this::toFavoriteVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, FavoriteToggleRequest request) {
        if (userFavoriteRepository.findByUserIdAndProductId(userId, request.getProductId()).isPresent()) {
            return;
        }
        UserFavorite favorite = UserFavorite.builder()
                .userId(userId)
                .productId(request.getProductId())
                .productName(request.getProductName())
                .productImage(request.getProductImage())
                .price(request.getPrice())
                .build();
        userFavoriteRepository.save(favorite);
        log.info("添加收藏 - userId={}, productId={}", userId, request.getProductId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long productId) {
        userFavoriteRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("取消收藏 - userId={}, productId={}", userId, productId);
    }

    // ==================== 浏览历史 ====================

    @Transactional(readOnly = true)
    public List<BrowseHistoryVO> listBrowseHistory(Long userId, int limit) {
        List<BrowseHistory> historyList = browseHistoryRepository.findByUserIdOrderByViewedTimeDesc(userId);
        return historyList.stream()
                .limit(Math.min(Math.max(limit, 1), 50))
                .map(this::toBrowseHistoryVO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBrowseHistory(Long userId, FavoriteToggleRequest request) {
        BrowseHistory existing = browseHistoryRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);
        if (existing != null) {
            existing.setViewedTime(LocalDateTime.now());
            existing.setProductName(request.getProductName());
            existing.setProductImage(request.getProductImage());
            existing.setPrice(request.getPrice());
            browseHistoryRepository.save(existing);
        } else {
            BrowseHistory history = BrowseHistory.builder()
                    .userId(userId)
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .productImage(request.getProductImage())
                    .price(request.getPrice())
                    .viewedTime(LocalDateTime.now())
                    .build();
            browseHistoryRepository.save(history);
        }
        log.info("记录浏览历史 - userId={}, productId={}", userId, request.getProductId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearBrowseHistory(Long userId) {
        browseHistoryRepository.deleteByUserId(userId);
        log.info("清空浏览历史 - userId={}", userId);
    }

    // ==================== 库存校验 ====================

    @Transactional(readOnly = true)
    public Integer getStock(Long skuId) {
        return inventoryStockRepository.findBySkuId(skuId)
                .map(InventoryStock::getAvailableQuantity)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public boolean checkStock(Long skuId, Integer quantity) {
        // 无库存记录（存量商品）视为"库存未知"：放行，由下单时后端做真实库存校验
        if (!inventoryStockRepository.existsBySkuId(skuId)) {
            return true;
        }
        int stock = getStock(skuId);
        return stock >= quantity;
    }

    // ==================== 私有方法 ====================

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(33003, "用户不存在"));
    }

    private UserAddress findAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(33004, "地址不存在"));
        if (!address.getUserId().equals(userId) || Boolean.TRUE.equals(address.getDeleted())) {
            throw new BusinessException(33004, "地址不存在");
        }
        return address;
    }

    private void clearDefaultAddress(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserIdAndDeletedFalseOrderByIsDefaultDescCreatedTimeDesc(userId);
        for (UserAddress addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                userAddressRepository.save(addr);
            }
        }
    }

    private AddressVO toAddressVO(UserAddress a) {
        AddressVO vo = new AddressVO();
        vo.setId(a.getId());
        vo.setReceiver(a.getReceiver());
        vo.setPhone(a.getPhone());
        vo.setProvince(a.getProvince());
        vo.setCity(a.getCity());
        vo.setDistrict(a.getDistrict());
        vo.setDetailAddress(a.getDetailAddress());
        vo.setPostalCode(a.getPostalCode());
        vo.setIsDefault(a.getIsDefault());
        return vo;
    }

    private CouponVO toCouponVO(UserCoupon c) {
        CouponVO vo = new CouponVO();
        vo.setId(c.getId());
        vo.setCouponName(c.getCouponName());
        vo.setCouponType(c.getCouponType());
        vo.setDiscountAmount(c.getDiscountAmount());
        vo.setMinAmount(c.getMinAmount());
        vo.setStatus(c.getStatus());
        vo.setExpireTime(c.getExpireTime());
        vo.setCreatedTime(c.getCreatedTime());
        return vo;
    }

    private FavoriteVO toFavoriteVO(UserFavorite f) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(f.getId());
        vo.setProductId(f.getProductId());
        vo.setProductName(f.getProductName());
        vo.setProductImage(f.getProductImage());
        vo.setPrice(f.getPrice());
        vo.setCreatedTime(f.getCreatedTime());
        return vo;
    }

    private BrowseHistoryVO toBrowseHistoryVO(BrowseHistory h) {
        BrowseHistoryVO vo = new BrowseHistoryVO();
        vo.setId(h.getId());
        vo.setProductId(h.getProductId());
        vo.setProductName(h.getProductName());
        vo.setProductImage(h.getProductImage());
        vo.setPrice(h.getPrice());
        vo.setViewedTime(h.getViewedTime());
        return vo;
    }
}