package com.commerce.platform.ai.controller;

import com.commerce.platform.ai.config.AiGatewayProperties;
import com.commerce.platform.ai.exception.AiGatewayException;
import com.commerce.platform.common.entity.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Admin AI 中心统计 Controller
 * <p>
 * 转发 ai-service 的用量统计接口（/api/v1/internal/ai/stats），
 * 供 admin 端 AI 中心与仪表盘实时展示调用量。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiStatsController {

    private static final String STATS_PATH = "/api/v1/internal/ai/stats";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AiGatewayProperties properties;

    public AdminAiStatsController(HttpClient aiServiceHttpClient,
                                  ObjectMapper objectMapper,
                                  AiGatewayProperties properties) {
        this.httpClient = aiServiceHttpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<JsonNode> getAiStats() {
        if (properties.baseUrl() == null || properties.internalToken() == null
                || properties.internalToken().isBlank()) {
            throw new AiGatewayException("AI Gateway authentication is not configured");
        }

        URI uri = URI.create(properties.baseUrl().toString().replaceAll("/$", "") + STATS_PATH);
        HttpRequest upstreamRequest = HttpRequest.newBuilder(uri)
                .timeout(properties.requestTimeout())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .header("X-Internal-Token", properties.internalToken())
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(upstreamRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new AiGatewayException("AI Service stats returned status " + response.statusCode());
            }
            JsonNode body = objectMapper.readTree(response.body());
            return Result.success(body.has("data") ? body.get("data") : body);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiGatewayException("AI Service stats request was interrupted", e);
        } catch (IOException e) {
            throw new AiGatewayException("AI Service is unavailable for stats", e);
        }
    }
}