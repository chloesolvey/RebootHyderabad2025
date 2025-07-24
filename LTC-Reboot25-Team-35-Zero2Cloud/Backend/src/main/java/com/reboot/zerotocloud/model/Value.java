package com.reboot.zerotocloud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Value(
    @JsonProperty("doubleValue") Double doubleValue,
    @JsonProperty("int64Value") Long int64Value
) {}
