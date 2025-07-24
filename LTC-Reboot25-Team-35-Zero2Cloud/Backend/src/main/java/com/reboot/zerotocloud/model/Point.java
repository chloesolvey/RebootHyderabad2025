package com.reboot.zerotocloud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Point(
    @JsonProperty("interval") Interval interval,
    @JsonProperty("value") Value value
) {}
