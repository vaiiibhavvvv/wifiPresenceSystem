package com.presence.api.service;

import com.presence.api.dto.PresenceResponse;
import com.presence.api.model.PresenceEvent;
import com.presence.api.model.PresenceStatus;
import com.presence.api.repository.PresenceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate     redis;
    private final PresenceEventRepository repo;

    // ── Redis key templates ──────────────────────────────
    private static final String KEY_STATUS    = "presence:%s:status";
    private static final String KEY_LAST_SEEN = "presence:%s:lastSeen";
    private static final String KEY_LAST_RSSI = "presence:%s:lastRssi";

    // ── Current state ────────────────────────────────────
    /**
     * Returns the current presence state for a device.
     * Reads from Redis first (< 1ms). Falls back to PostgreSQL
     * if Redis is cold (e.g., after a restart).
     */
    public PresenceResponse getCurrentPresence(String deviceId) {
        String statusStr   = redis.opsForValue().get(String.format(KEY_STATUS,    deviceId));
        String lastSeenStr = redis.opsForValue().get(String.format(KEY_LAST_SEEN, deviceId));
        String lastRssiStr = redis.opsForValue().get(String.format(KEY_LAST_RSSI, deviceId));

        // Fallback to DB if Redis has nothing
        if (statusStr == null) {
            log.debug("Redis miss for device={}, falling back to DB", deviceId);
            Optional<PresenceEvent> latest = repo.findTopByDeviceIdOrderByDetectedAtDesc(deviceId);
            if (latest.isPresent()) {
                PresenceEvent e = latest.get();
                statusStr   = e.getStatus().name();
                lastSeenStr = e.getDetectedAt() != null ? String.valueOf(e.getDetectedAt()) : null;
                lastRssiStr = e.getAvgRssi()    != null ? String.valueOf(e.getAvgRssi().intValue()) : null;
            }
        }

        return PresenceResponse.builder()
                .deviceId(deviceId)
                .status(statusStr != null ? statusStr : PresenceStatus.UNCERTAIN.name())
                .lastDetected(lastSeenStr != null ? Long.parseLong(lastSeenStr) : null)
                .lastRssi(lastRssiStr     != null ? Double.parseDouble(lastRssiStr) : null)
                .serverTime(Instant.now().getEpochSecond())
                .build();
    }

    /**
     * Current state for ALL known devices.
     * Used by the WebSocket broadcaster and the /presence/all endpoint.
     */
    public List<PresenceResponse> getAllCurrentStates() {
        List<String> deviceIds = repo.findAllDeviceIds();
        if (deviceIds.isEmpty()) {
            // Return a default entry so the frontend has something to show
            return List.of(getCurrentPresence("default"));
        }
        return deviceIds.stream()
                .map(this::getCurrentPresence)
                .toList();
    }

    // ── History ──────────────────────────────────────────
    /**
     * Returns paginated historical events for a device from PostgreSQL.
     */
    public Map<String, Object> getHistory(String deviceId, long from, long to, int limit) {
        int safeLimit = Math.min(limit, 1000);

        List<PresenceEvent> events = repo.findHistory(
                deviceId, from, to, PageRequest.of(0, safeLimit));

        List<Map<String, Object>> eventDtos = events.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status",     e.getStatus().name());
            m.put("movement",   e.isMovement());
            m.put("variance",   e.getVariance());
            m.put("avgRssi",    e.getAvgRssi());
            m.put("detectedAt", e.getDetectedAt());
            return m;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deviceId", deviceId);
        response.put("from",     from);
        response.put("to",       to);
        response.put("total",    eventDtos.size());
        response.put("events",   eventDtos);
        return response;
    }

    // ── Stats ────────────────────────────────────────────
    /**
     * Occupancy statistics for a device over a time range.
     */
    public Map<String, Object> getStats(String deviceId, long from, long to) {
        List<PresenceEvent> movements = repo.findMovementEvents(deviceId, from, to);

        long totalEvents   = repo.findHistory(deviceId, from, to, PageRequest.of(0, 10000)).size();
        long occupiedCount = repo.countByDeviceIdAndStatusAndDetectedAtBetween(
                deviceId, PresenceStatus.OCCUPIED, from, to);

        double occupancyRate = totalEvents == 0
                ? 0.0
                : Math.round((double) occupiedCount / totalEvents * 1000.0) / 10.0;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deviceId",      deviceId);
        response.put("from",          from);
        response.put("to",            to);
        response.put("totalEvents",   totalEvents);
        response.put("occupiedCount", occupiedCount);
        response.put("movementCount", (long) movements.size());
        response.put("occupancyRate", occupancyRate);
        return response;
    }

    // ── Device list ──────────────────────────────────────
    public Map<String, Object> getAllDevices() {
        List<String> devices = repo.findAllDeviceIds();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count",     devices.size());
        response.put("deviceIds", devices);
        return response;
    }
}