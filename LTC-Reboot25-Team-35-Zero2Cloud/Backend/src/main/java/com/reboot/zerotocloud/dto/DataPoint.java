package com.reboot.zerotocloud.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DataPoint {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime timestamp;
    private double value;

    public DataPoint() {}

    public DataPoint(OffsetDateTime ts, double v) {
        this.timestamp = ts;
        this.value = v;
    }

    // getters & setters
}

