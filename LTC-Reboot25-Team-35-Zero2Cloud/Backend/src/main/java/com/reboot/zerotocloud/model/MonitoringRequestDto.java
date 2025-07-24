package com.reboot.zerotocloud.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MonitoringRequestDto {

	@NotBlank(message = "Start time is required")
    private String startTime; // RFC 3339 format

    @NotBlank(message = "End time is required")
    private String endTime;   // RFC 3339 format

    @NotBlank(message = "Resource ID is required")
    private String resourceId;
}
