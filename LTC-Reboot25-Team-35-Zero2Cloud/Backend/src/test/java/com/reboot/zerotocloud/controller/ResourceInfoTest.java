package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.model.GcpVmInfo;
import com.reboot.zerotocloud.service.ResourceInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResourcesInfoTest {

    private MockMvc mockMvc;

    @Mock
    private ResourceInfoService resourceInfoService;

    private ResourcesInfo controller;

    @BeforeEach
    void setUp() {
        controller = new ResourcesInfo();
        ReflectionTestUtils.setField(controller, "resourceInfoService", resourceInfoService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .build();
    }

    @Test
    void getAllVmInfo_returnsListOfVmInfo() throws Exception {
        GcpVmInfo vm1 = new GcpVmInfo();
        vm1.setName("vm1");
        vm1.setStatus("RUNNING");
        vm1.setMachineType("n1-standard-1");
        vm1.setZone("us-central1-a");
        vm1.setOs("debian-11");
        vm1.setCreatedTime("2025-07-24T05:00:00Z");
        vm1.setMemoryUsage("2GB");

        GcpVmInfo vm2 = new GcpVmInfo();
        vm2.setName("vm2");
        vm2.setStatus("TERMINATED");
        vm2.setMachineType("n1-standard-2");
        vm2.setZone("us-central1-b");
        vm2.setOs("ubuntu-20.04");
        vm2.setCreatedTime("2025-07-23T04:30:00Z");
        vm2.setMemoryUsage("4GB");

        List<GcpVmInfo> vms = List.of(vm1, vm2);
        when(resourceInfoService.getAllVmInfo()).thenReturn(ResponseEntity.ok(vms));

        mockMvc.perform(get("/api/gcp/all-vms"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("vm1"))
            .andExpect(jsonPath("$[0].status").value("RUNNING"))
            .andExpect(jsonPath("$[0].machineType").value("n1-standard-1"))
            .andExpect(jsonPath("$[0].zone").value("us-central1-a"))
            .andExpect(jsonPath("$[0].os").value("debian-11"))
            .andExpect(jsonPath("$[0].createdTime").value("2025-07-24T05:00:00Z"))
            .andExpect(jsonPath("$[0].memoryUsage").value("2GB"))
            .andExpect(jsonPath("$[1].name").value("vm2"))
            .andExpect(jsonPath("$[1].status").value("TERMINATED"))
            .andExpect(jsonPath("$[1].machineType").value("n1-standard-2"))
            .andExpect(jsonPath("$[1].zone").value("us-central1-b"))
            .andExpect(jsonPath("$[1].os").value("ubuntu-20.04"))
            .andExpect(jsonPath("$[1].createdTime").value("2025-07-23T04:30:00Z"))
            .andExpect(jsonPath("$[1].memoryUsage").value("4GB"));

        verify(resourceInfoService).getAllVmInfo();
    }

}