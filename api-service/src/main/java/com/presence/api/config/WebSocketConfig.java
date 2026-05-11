package com.presence.api.config;

import com.presence.api.dto.PresenceResponse;
import com.presence.api.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// ── STOMP WebSocket config ───────────────────────────────
@Configuration
@EnableWebSocketMessageBroker
class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client subscribes to /topic/...
        registry.enableSimpleBroker("/topic");
        // Client sends to /app/...
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();   // fallback for browsers without native WebSocket
    }
}

// ── Scheduled broadcaster ────────────────────────────────
@Slf4j
@Component
@RequiredArgsConstructor
class PresenceBroadcaster {

    private final SimpMessagingTemplate messaging;
    private final PresenceService       presenceService;

    @Value("${app.websocket.push-interval-ms:2000}")
    private long pushIntervalMs;

    /**
     * Every 2 seconds, push the current state of all known devices
     * to all WebSocket subscribers on /topic/presence.
     *
     * Frontend subscribes to this and updates the dashboard in real time.
     */
    @Scheduled(fixedDelayString = "${app.websocket.push-interval-ms:2000}")
    public void broadcast() {
        try {
            List<PresenceResponse> states = presenceService.getAllCurrentStates();

            Map<String, Object> payload = Map.of(
                    "timestamp", Instant.now().toEpochMilli(),
                    "devices",   states
            );

            messaging.convertAndSend("/topic/presence", payload);
            log.debug("WebSocket broadcast: {} devices", states.size());

        } catch (Exception ex) {
            log.warn("WebSocket broadcast failed: {}", ex.getMessage());
        }
    }
}