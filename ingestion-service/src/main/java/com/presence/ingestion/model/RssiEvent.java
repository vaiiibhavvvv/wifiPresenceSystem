package com.presence.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RssiEvent {

    @NotBlank(message = "deviceId must not be blank")
    @Size(max = 64)
    @JsonProperty("deviceId")
    private String deviceId;

    @NotNull
    @Min(-120)
    @Max(0)
    @JsonProperty("rssi")
    private Integer rssi;

    @NotNull
   @Positive
    @JsonProperty("timestamp")
    private Long timestamp;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Long receivedAt) {
        this.receivedAt = receivedAt;
    }

    private Long receivedAt;


}
