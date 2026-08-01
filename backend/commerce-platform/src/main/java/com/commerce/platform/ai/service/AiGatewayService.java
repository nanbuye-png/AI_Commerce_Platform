package com.commerce.platform.ai.service;

import com.commerce.platform.ai.config.AiGatewayProperties;
import com.commerce.platform.ai.dto.ChatStreamRequest;
import com.commerce.platform.ai.exception.AiGatewayException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class AiGatewayService {

    private static final String CHAT_STREAM_PATH = "/api/v1/internal/ai/chat/stream";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AiGatewayProperties properties;

    public AiGatewayService(HttpClient aiServiceHttpClient,
                            ObjectMapper objectMapper,
                            AiGatewayProperties properties) {
        this.httpClient = aiServiceHttpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void stream(ChatStreamRequest request, Consumer<String> eventConsumer) {
        ensureConfigured();

        HttpRequest upstreamRequest = HttpRequest.newBuilder(resolveChatUri())
                .timeout(properties.requestTimeout())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Accept", MediaType.TEXT_EVENT_STREAM_VALUE)
                .header("X-Internal-Token", properties.internalToken())
                .POST(HttpRequest.BodyPublishers.ofString(serialize(request)))
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(
                    upstreamRequest,
                    HttpResponse.BodyHandlers.ofInputStream()
            );
            if (response.statusCode() != 200) {
                throw new AiGatewayException("AI Service returned status " + response.statusCode());
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                relayEvents(reader, eventConsumer);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiGatewayException("AI Service request was interrupted", e);
        } catch (IOException e) {
            throw new AiGatewayException("AI Service is unavailable", e);
        }
    }

    private void relayEvents(BufferedReader reader, Consumer<String> eventConsumer) throws IOException {
        StringBuilder event = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (!event.isEmpty()) {
                    eventConsumer.accept(event.append("\n").toString());
                    event.setLength(0);
                }
                continue;
            }
            event.append(line).append("\n");
        }
        if (!event.isEmpty()) {
            eventConsumer.accept(event.append("\n").toString());
        }
    }

    private String serialize(ChatStreamRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new AiGatewayException("Unable to serialize AI request", e);
        }
    }

    private URI resolveChatUri() {
        return URI.create(properties.baseUrl().toString().replaceAll("/$", "") + CHAT_STREAM_PATH);
    }

    private void ensureConfigured() {
        if (properties.baseUrl() == null || properties.internalToken() == null
                || properties.internalToken().isBlank()) {
            throw new AiGatewayException("AI Gateway authentication is not configured");
        }
        Objects.requireNonNull(properties.connectTimeout(), "AI connect timeout must be configured");
        Objects.requireNonNull(properties.requestTimeout(), "AI request timeout must be configured");
    }
}