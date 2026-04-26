package com.presence.processing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "presence_events")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PresenceEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private PresenceStatus status;

    @Column(name = "movement", nullable = false)
    private boolean movement;

    @Column(name = "variance")
    private Double variance;

    @Column(name = "avg_rssi")
    private Double avgRssi;

    @Column(name = "detected_at", nullable = false)
    private Long detectedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}