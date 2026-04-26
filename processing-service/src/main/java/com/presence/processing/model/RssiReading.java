package com.presence.processing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rssi_readings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RssiReading {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "rssi", nullable = false)
    private Integer rssi;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}