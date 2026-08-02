package com.commerce.platform.profile.repository;

import com.commerce.platform.profile.entity.BrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 浏览历史 Repository
 */
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {

    List<BrowseHistory> findTop20ByUserIdOrderByViewedTimeDesc(Long userId);

    List<BrowseHistory> findByUserIdOrderByViewedTimeDesc(Long userId);

    Optional<BrowseHistory> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);
}