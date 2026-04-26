package com.presence.processing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class PresenceExpiryScheduler {

    private final StringRedisTemplate redis;
    private final PresenceStateService presenceStateService;

    @Scheduled(fixedDelay = 10_000)
    public void expireStalePresence() {
        Set<String> keys = redis.keys("presence:*:lastSeen");
        if (keys == null || keys.isEmpty()) return;
        keys.forEach(key -> {
            String[] parts = key.split(":");
            if (parts.length >= 3) presenceStateService.checkAndExpirePresence(parts[1]);
        });
    }
}