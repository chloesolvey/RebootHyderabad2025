package com.reboot.zerotocloud.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonitoringRequest {

    @NotNull
    private TimeframeType timeframe;

    // Required when timeframe==CUSTOM
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Getters/Setters omitted for brevity

    @AssertTrue(message = "startTime and endTime must be provided for CUSTOM timeframe")
    private boolean isCustomRangeValid() {
        if (timeframe == TimeframeType.CUSTOM) {
            return startTime != null && endTime != null && startTime.isBefore(endTime);
        }
        return true;
    }
}

