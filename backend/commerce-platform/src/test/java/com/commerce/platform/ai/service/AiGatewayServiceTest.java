package com.commerce.platform.ai.service;

import com.commerce.platform.ai.config.AiGatewayProperties;
import com.commerce.platform.ai.dto.ChatStreamRequest;
import com.commerce.platform.ai.exception.AiGatewayException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiGatewayServiceTest {

    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void relaysSseEventsAndAuthenticatesToAiService() throws Exception {
        AtomicReference<String> receivedToken = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server.createContext("/api/v1/internal/ai/chat/stream", exchange -> {
            receivedToken.set(exchange.getRequestHeaders().getFirst("X-Internal-Token"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200,
                    "event: message\ndata: {\"type\":\"token\",\"content\":\"好\"}\n\n"
                            + "event: done\ndata: {\"conversation_id\":\"conv_1\",\"message_id\":\"msg_1\"}\n\n");
        });

        List<String> events = new ArrayList<>();
        service("test-internal-token").stream(new ChatStreamRequest("推荐耳机", "conv_1"), events::add);

        assertEquals("test-internal-token", receivedToken.get());
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(
                objectMapper.readTree("{\"message\":\"推荐耳机\",\"conversation_id\":\"conv_1\"}"),
                objectMapper.readTree(receivedBody.get())
        );
        assertEquals(List.of(
                "event: message\ndata: {\"type\":\"token\",\"content\":\"好\"}\n\n",
                "event: done\ndata: {\"conversation_id\":\"conv_1\",\"message_id\":\"msg_1\"}\n\n"
        ), events);
    }

    @Test
    void rejectsMissingInternalAuthenticationConfiguration() {
        AiGatewayException exception = assertThrows(AiGatewayException.class,
                () -> service("").stream(new ChatStreamRequest("hello", null), ignored -> { }));

        assertEquals("AI Gateway authentication is not configured", exception.getMessage());
    }

    @Test
    void rejectsNonSuccessfulUpstreamResponse() {
        server.createContext("/api/v1/internal/ai/chat/stream", exchange -> respond(exchange, 401, "unauthorized"));

        AiGatewayException exception = assertThrows(AiGatewayException.class,
                () -> service("wrong-token").stream(new ChatStreamRequest("hello", null), ignored -> { }));

        assertEquals("AI Service returned status 401", exception.getMessage());
    }

    private AiGatewayService service(String internalToken) {
        URI baseUrl = URI.create("http://localhost:" + server.getAddress().getPort());
        AiGatewayProperties properties = new AiGatewayProperties(
                baseUrl, internalToken, Duration.ofSeconds(1), Duration.ofSeconds(5));
        return new AiGatewayService(HttpClient.newHttpClient(), new ObjectMapper(), properties);
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}