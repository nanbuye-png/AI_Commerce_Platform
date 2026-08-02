package com.commerce.platform.profile.repository;

import com.commerce.platform.profile.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户收藏夹 Repository
 */
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    Page<UserFavorite> findByUserId(Long userId, Pageable pageable);

    Optional<UserFavorite> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}