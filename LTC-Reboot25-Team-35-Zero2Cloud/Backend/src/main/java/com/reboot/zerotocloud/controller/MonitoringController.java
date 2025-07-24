package com.reboot.zerotocloud.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reboot.zerotocloud.dto.MonitoringRequest;
import com.reboot.zerotocloud.dto.MonitoringResponse;
import com.reboot.zerotocloud.service.GCPMonitoringService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1")
@Validated
public class MonitoringController {

    private final GCPMonitoringService service;

    public MonitoringController(GCPMonitoringService service) {
        this.service = service;
    }

    @PostMapping(path = "/vm/cpu", produces = MediaType.APPLICATION_JSON_VALUE)
    public MonitoringResponse getCpu(
        @RequestBody @Valid MonitoringRequest req
    ) throws IOException {
        return service.getCpuUtilization(req);
    }

    @PostMapping(path = "/vm/memory", produces = MediaType.APPLICATION_JSON_VALUE)
    public MonitoringResponse getMemory(
        @RequestBody @Valid MonitoringRequest req
    ) throws IOException {
        return service.getMemoryUtilization(req);
    }

    @PostMapping(path = "/bucket/bytes", produces = MediaType.APPLICATION_JSON_VALUE)
    public MonitoringResponse getTotalBytes(
        @RequestBody @Valid MonitoringRequest req
    ) throws IOException {
        return service.getBucketBytes(req);
    }

    @PostMapping(path = "/bucket/objects", produces = MediaType.APPLICATION_JSON_VALUE)
    public MonitoringResponse getObjectCount(
        @RequestBody @Valid MonitoringRequest req
    ) throws IOException {
        return service.getBucketObjectCount(req);
    }

    @PostMapping(path = "/cloudsql/cpu", produces = MediaType.APPLICATION_JSON_VALUE)
    public MonitoringResponse getCloudSqlCpu(
        @RequestBody @Valid MonitoringRequest req
    ) throws IOException {
        return service.getCloudSqlCpu(req);
    }

    @PostMapping(path = "/cloudsql/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public MonitoringResponse getConnections(
        @RequestBody @Valid MonitoringRequest req
    ) throws IOException {
        return service.getCloudSqlConnections(req);
    }

}
