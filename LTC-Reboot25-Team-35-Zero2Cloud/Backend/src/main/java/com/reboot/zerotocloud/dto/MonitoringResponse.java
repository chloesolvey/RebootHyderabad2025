package com.reboot.zerotocloud.dto;

import java.util.List;

import lombok.Data;

@Data
public class MonitoringResponse {
    private List<DataPoint> points;
    // getters & setters
}

