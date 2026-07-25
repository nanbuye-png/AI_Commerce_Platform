package com.commerce.platform.module.health;

import com.commerce.platform.common.entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查接口
 * 用于 Kubernetes 探活 / 前端判断服务状态
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.application.name:commerce-platform}")
    private String appName;

    @GetMapping
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "UP",
                "app", appName,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}