package com.commerce.platform.user.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.enums.UserRole;
import com.commerce.platform.user.enums.UserStatus;
import com.commerce.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin 商家管理 Controller
 * <p>
 * 只允许 ADMIN 角色访问。
 * 商家即 role=MERCHANT 的用户。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

    private final UserRepository userRepository;

    /**
     * 商家列表（role=MERCHANT 的用户）
     */
    @GetMapping
    public Result<Page<User>> listMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        log.info("Admin 商家列表查询 - page={}, size={}, status={}, keyword={}", page, pageSize, status, keyword);

        Specification<User> spec = (root, query, cb) -> {
            jakarta.persistence.criteria.Predicate rolePred = cb.equal(root.get("role"), UserRole.MERCHANT);
            if (status != null && !status.isEmpty()) {
                try {
                    UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
                    return cb.and(rolePred, cb.equal(root.get("status"), userStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的状态参数: {}", status);
                }
            }
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate keywordPred = cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("nickname")), pattern)
                );
                return cb.and(rolePred, keywordPred);
            }
            return rolePred;
        };

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<User> merchantPage = userRepository.findAll(spec, pageRequest);
        log.info("Admin 商家列表查询完成 - total={}", merchantPage.getTotalElements());
        return Result.success(merchantPage);
    }

    /**
     * 商家详情
     */
    @GetMapping("/{id}")
    public Result<User> getMerchantDetail(@PathVariable Long id) {
        log.info("Admin 商家详情查询 - id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商家不存在: " + id));
        if (user.getRole() != UserRole.MERCHANT) {
            return Result.error("该用户不是商家");
        }
        return Result.success(user);
    }

    /**
     * 商家状态管理（启用/停用/锁定）
     */
    @PutMapping("/{id}/status")
    public Result<User> updateMerchantStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        log.info("Admin 更新商家状态 - id={}, status={}", id, newStatus);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商家不存在: " + id));

        if (user.getRole() != UserRole.MERCHANT) {
            return Result.error("该用户不是商家");
        }

        try {
            user.setStatus(UserStatus.valueOf(newStatus.toUpperCase()));
            User saved = userRepository.save(user);
            log.info("Admin 商家状态更新成功 - id={}, status={}", id, saved.getStatus());
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.error("无效的状态值: " + newStatus);
        }
    }
}