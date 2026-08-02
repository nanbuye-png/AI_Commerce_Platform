package com.commerce.platform.profile.repository;

import com.commerce.platform.profile.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 收货地址 Repository
 */
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdAndDeletedFalseOrderByIsDefaultDescCreatedTimeDesc(Long userId);
}