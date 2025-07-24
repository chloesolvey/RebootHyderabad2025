package com.reboot.zerotocloud.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AccessPolicyDTO {
    private String role;
    private Set<String> members;
    private String scope;
    private String description;
}