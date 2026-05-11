package com.presence.api.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

// ── Current presence state for one device ────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceResponse {
    private String  deviceId;
    private String  status;
    private Long    lastDetected;
    private Double  lastRssi;
    private Long    serverTime;
}