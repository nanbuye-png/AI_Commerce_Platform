package com.commerce.platform.profile.repository;

import com.commerce.platform.profile.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户优惠券 Repository
 */
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserId(Long userId);

    List<UserCoupon> findByUserIdAndStatus(Long userId, String status);
}