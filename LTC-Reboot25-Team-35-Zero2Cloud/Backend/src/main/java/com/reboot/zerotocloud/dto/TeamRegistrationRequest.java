package com.reboot.zerotocloud.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRegistrationRequest {
    private String projectId;
    private String labelKey;
    private String labelValue;
}