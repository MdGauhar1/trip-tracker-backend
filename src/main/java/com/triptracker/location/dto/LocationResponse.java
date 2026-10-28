
package com.triptracker.location.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LocationResponse {

    private final Long userId;
    private final String userName;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final BigDecimal accuracy;
    private final LocalDateTime recordedAt;

    public LocationResponse(
            Long userId,
            String userName,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracy,
            LocalDateTime recordedAt) {
        this.userId = userId;
        this.userName = userName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.recordedAt = recordedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
}
