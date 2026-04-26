package com.presence.processing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RssiEvent {
    @JsonProperty("deviceId")  private String  deviceId;
    @JsonProperty("rssi")      private Integer rssi;
    @JsonProperty("timestamp") private Long    timestamp;
    private Long receivedAt;
}