package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.model.GcpVmInfo;
import com.reboot.zerotocloud.service.GCPProvisionService;
import com.reboot.zerotocloud.service.ResourceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/gcp")
public class ResourcesInfo {

    @Autowired
    private ResourceInfoService resourceInfoService;

    @GetMapping("/all-vms")
    public ResponseEntity<List<GcpVmInfo>> getAllVmInfo() throws IOException {
        try {
            return resourceInfoService.getAllVmInfo();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch VM info", e);
        }
    }
}
