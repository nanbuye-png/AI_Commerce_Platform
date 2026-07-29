package com.commerce.platform.user.controller;

import com.commerce.platform.common.entity.Result;
import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.enums.UserRole;
import com.commerce.platform.user.enums.UserStatus;
import com.commerce.platform.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin 用户管理 Controller
 * <p>
 * 只允许 ADMIN 角色访问。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;

    /**
     * 用户分页查询
     */
    @GetMapping
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        int safeSize = Math.min(pageSize, MAX_PAGE_SIZE);
        log.info("Admin 用户列表查询 - page={}, size={}, role={}, status={}, keyword={}", page, safeSize, role, status, keyword);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null && !role.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("role"), UserRole.valueOf(role.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的角色参数: {}", role);
                }
            }
            if (status != null && !status.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("status"), UserStatus.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的状态参数: {}", status);
                }
            }
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate usernameLike = cb.like(cb.lower(root.get("username")), pattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
                Predicate nicknameLike = cb.like(cb.lower(root.get("nickname")), pattern);
                predicates.add(cb.or(usernameLike, emailLike, nicknameLike));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<User> userPage = userRepository.findAll(spec, pageRequest);
        log.info("Admin 用户列表查询完成 - total={}", userPage.getTotalElements());
        return Result.success(userPage);
    }

    /**
     * 用户详情查询
     */
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        log.info("Admin 用户详情查询 - id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
        return Result.success(user);
    }

    /**
     * 用户状态管理（启用/停用/锁定）
     */
    @PutMapping("/{id}/status")
    public Result<User> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        log.info("Admin 更新用户状态 - id={}, status={}", id, newStatus);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));

        try {
            UserStatus status = UserStatus.valueOf(newStatus.toUpperCase());
            user.setStatus(status);
            User saved = userRepository.save(user);
            log.info("Admin 用户状态更新成功 - id={}, status={}", id, saved.getStatus());
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.error("无效的状态值: " + newStatus);
        }
    }
}