package com.presence.api.controller;

import com.presence.api.dto.PresenceResponse;
import com.presence.api.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    /**
     * GET /presence?deviceId=xxx
     * Current presence state — reads Redis first, falls back to DB.
     */
    @GetMapping("/presence")
    public ResponseEntity<PresenceResponse> getPresence(
            @RequestParam(defaultValue = "default") String deviceId) {

        log.debug("GET /presence deviceId={}", deviceId);
        return ResponseEntity.ok(presenceService.getCurrentPresence(deviceId));
    }

    /**
     * GET /presence/all
     * Current state for every known device.
     */
    @GetMapping("/presence/all")
    public ResponseEntity<List<PresenceResponse>> getAllPresence() {
        return ResponseEntity.ok(presenceService.getAllCurrentStates());
    }

    /**
     * GET /history?deviceId=xxx&from=epoch&to=epoch&limit=300
     * Historical events from PostgreSQL.
     *
     * from/to are Unix epoch seconds.
     * Defaults to the last 1 hour if not provided.
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "default") String deviceId,
            @RequestParam(required = false)          Long   from,
            @RequestParam(required = false)          Long   to,
            @RequestParam(defaultValue = "300")      int    limit) {

        long now  = Instant.now().getEpochSecond();
        long toTs = to   != null ? to   : now;
        long fromTs = from != null ? from : now - 3600; // default: last 1 hour

        log.debug("GET /history deviceId={} from={} to={} limit={}", deviceId, fromTs, toTs, limit);
        return ResponseEntity.ok(presenceService.getHistory(deviceId, fromTs, toTs, limit));
    }

    /**
     * GET /stats?deviceId=xxx&from=epoch&to=epoch
     * Occupancy statistics — movement count, occupancy rate, etc.
     * Defaults to the last 24 hours if not provided.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(defaultValue = "default") String deviceId,
            @RequestParam(required = false)          Long   from,
            @RequestParam(required = false)          Long   to) {

        long now  = Instant.now().getEpochSecond();
        long toTs = to   != null ? to   : now;
        long fromTs = from != null ? from : now - 86400; // default: last 24 hours

        log.debug("GET /stats deviceId={} from={} to={}", deviceId, fromTs, toTs);
        return ResponseEntity.ok(presenceService.getStats(deviceId, fromTs, toTs));
    }

    /**
     * GET /devices
     * All device IDs that have ever sent data.
     */
    @GetMapping("/devices")
    public ResponseEntity<Map<String, Object>> getDevices() {
        return ResponseEntity.ok(presenceService.getAllDevices());
    }

    /**
     * GET /ping
     * Health check — no DB or Redis dependency.
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "api-service",
                "time",    String.valueOf(Instant.now().getEpochSecond())
        ));
    }
}