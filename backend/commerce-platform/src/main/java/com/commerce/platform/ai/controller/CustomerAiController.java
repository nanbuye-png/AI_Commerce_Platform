package com.commerce.platform.ai.controller;

import com.commerce.platform.ai.dto.ChatStreamRequest;
import com.commerce.platform.ai.service.AiGatewayService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/customer/ai")
public class CustomerAiController {

    private final AiGatewayService aiGatewayService;

    public CustomerAiController(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void streamChat(@Valid @RequestBody ChatStreamRequest request,
                           HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        aiGatewayService.stream(request, event -> writeEvent(response, event));
    }

    private void writeEvent(HttpServletResponse response, String event) {
        try {
            response.getWriter().write(event);
            response.getWriter().flush();
        } catch (IOException e) {
            throw new UncheckedIOException("Client disconnected from AI stream", e);
        }
    }
}