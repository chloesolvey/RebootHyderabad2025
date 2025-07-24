package com.reboot.zerotocloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reboot.zerotocloud.dto.DataPoint;
import com.reboot.zerotocloud.dto.MonitoringRequest;
import com.reboot.zerotocloud.dto.MonitoringResponse;
import com.reboot.zerotocloud.dto.TimeframeType;
import com.reboot.zerotocloud.service.GCPMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GCPMonitoringService service;

    private MonitoringController controller;

    @BeforeEach
    void setUp() {
        controller = new MonitoringController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private MonitoringRequest buildCustomRequest() {
        MonitoringRequest req = new MonitoringRequest();
        req.setTimeframe(TimeframeType.CUSTOM);
        req.setStartTime(LocalDateTime.of(2025, 7, 24, 0, 0));
        req.setEndTime(LocalDateTime.of(2025, 7, 24, 1, 0));
        return req;
    }

    private MonitoringResponse buildResponse(double value) {
        MonitoringResponse resp = new MonitoringResponse();
        DataPoint point = new DataPoint(
            OffsetDateTime.parse("2025-07-24T05:00:00Z"),
            value
        );
        resp.setPoints(List.of(point));
        return resp;
    }

    @Test
    void getCpu_returnsDataPoints() throws Exception {
        MonitoringRequest req = buildCustomRequest();
        MonitoringResponse resp = buildResponse(42.0);

        when(service.getCpuUtilization(any(MonitoringRequest.class)))
            .thenReturn(resp);

        mockMvc.perform(post("/api/v1/vm/cpu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.points[0].timestamp").value("2025-07-24T05:00:00Z"))
            .andExpect(jsonPath("$.points[0].value").value(42.0));

        verify(service).getCpuUtilization(any(MonitoringRequest.class));
    }

    @Test
    void getMemory_returnsDataPoints() throws Exception {
        MonitoringRequest req = buildCustomRequest();
        MonitoringResponse resp = buildResponse(73.5);

        when(service.getMemoryUtilization(any(MonitoringRequest.class)))
            .thenReturn(resp);

        mockMvc.perform(post("/api/v1/vm/memory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.points[0].value").value(73.5));

        verify(service).getMemoryUtilization(any(MonitoringRequest.class));
    }

    @Test
    void getTotalBytes_returnsDataPoints() throws Exception {
        MonitoringRequest req = buildCustomRequest();
        MonitoringResponse resp = buildResponse(1024.0);

        when(service.getBucketBytes(any(MonitoringRequest.class)))
            .thenReturn(resp);

        mockMvc.perform(post("/api/v1/bucket/bytes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.points[0].value").value(1024.0));

        verify(service).getBucketBytes(any(MonitoringRequest.class));
    }

    @Test
    void getObjectCount_returnsDataPoints() throws Exception {
        MonitoringRequest req = buildCustomRequest();
        MonitoringResponse resp = buildResponse(7.0);

        when(service.getBucketObjectCount(any(MonitoringRequest.class)))
            .thenReturn(resp);

        mockMvc.perform(post("/api/v1/bucket/objects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.points[0].value").value(7.0));

        verify(service).getBucketObjectCount(any(MonitoringRequest.class));
    }

    @Test
    void getCloudSqlCpu_returnsDataPoints() throws Exception {
        MonitoringRequest req = buildCustomRequest();
        MonitoringResponse resp = buildResponse(5.5);

        when(service.getCloudSqlCpu(any(MonitoringRequest.class)))
            .thenReturn(resp);

        mockMvc.perform(post("/api/v1/cloudsql/cpu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.points[0].value").value(5.5));

        verify(service).getCloudSqlCpu(any(MonitoringRequest.class));
    }

    @Test
    void getConnections_returnsDataPoints() throws Exception {
        MonitoringRequest req = buildCustomRequest();
        MonitoringResponse resp = buildResponse(12.0);

        when(service.getCloudSqlConnections(any(MonitoringRequest.class)))
            .thenReturn(resp);

        mockMvc.perform(post("/api/v1/cloudsql/connections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.points[0].value").value(12.0));

        verify(service).getCloudSqlConnections(any(MonitoringRequest.class));
    }
}