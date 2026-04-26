package com.presence.processing.service;

import com.presence.processing.model.PresenceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceStateService {

    private final StringRedisTemplate redis;

    @Value("${app.detection.presence-timeout-seconds:30}")
    private long presenceTimeoutSeconds;

    private static final String KEY_STATUS    = "presence:%s:status";
    private static final String KEY_LAST_SEEN = "presence:%s:lastSeen";
    private static final String KEY_LAST_RSSI = "presence:%s:lastRssi";

    public void updateState(String deviceId, boolean movement, double smoothedRssi, double variance) {
        long now = Instant.now().toEpochMilli();
        if (movement) {
            setStatus(deviceId, PresenceStatus.OCCUPIED);
            redis.opsForValue().set(key(KEY_LAST_SEEN, deviceId),
                    String.valueOf(now), Duration.ofSeconds(presenceTimeoutSeconds * 3));
            log.info("MOVEMENT detected device={} var={}", deviceId, variance);
        }
        redis.opsForValue().set(key(KEY_LAST_RSSI, deviceId),
                String.valueOf(Math.round(smoothedRssi)), Duration.ofSeconds(presenceTimeoutSeconds * 3));
    }

    public PresenceStatus getCurrentStatus(String deviceId) {
        String raw = redis.opsForValue().get(key(KEY_STATUS, deviceId));
        if (raw == null) return PresenceStatus.UNCERTAIN;
        try { return PresenceStatus.valueOf(raw); }
        catch (IllegalArgumentException e) { return PresenceStatus.UNCERTAIN; }
    }

    public Long getLastSeen(String deviceId) {
        String v = redis.opsForValue().get(key(KEY_LAST_SEEN, deviceId));
        return v != null ? Long.parseLong(v) : null;
    }

    public void setStatus(String deviceId, PresenceStatus status) {
        redis.opsForValue().set(key(KEY_STATUS, deviceId),
                status.name(), Duration.ofSeconds(presenceTimeoutSeconds * 3));
    }

    public void checkAndExpirePresence(String deviceId) {
        Long lastSeen = getLastSeen(deviceId);
        if (lastSeen == null) return;
        long elapsedSeconds = (Instant.now().toEpochMilli() - lastSeen) / 1000;
        if (elapsedSeconds > presenceTimeoutSeconds && getCurrentStatus(deviceId) == PresenceStatus.OCCUPIED) {
            setStatus(deviceId, PresenceStatus.EMPTY);
            log.info("Presence EXPIRED for device={} after {}s", deviceId, elapsedSeconds);
        }
    }

    private String key(String template, String deviceId) {
        return String.format(template, deviceId);
    }
}