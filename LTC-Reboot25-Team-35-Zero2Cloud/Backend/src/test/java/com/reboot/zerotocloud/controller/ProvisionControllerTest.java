package com.reboot.zerotocloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.compute.v1.Operation;
import com.reboot.zerotocloud.model.VMProvisionCriteria;
import com.reboot.zerotocloud.service.GCPProvisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProvisionControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private GCPProvisionService gcpProvisionService;

  private ProvisionController provisionController;

  @BeforeEach
  void setUp() {
    provisionController = new ProvisionController();
    ReflectionTestUtils.setField(
      provisionController, "gcpProvisionService", gcpProvisionService
    );
    mockMvc = MockMvcBuilders.standaloneSetup(provisionController).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void provisionVM_returnsOk_whenServiceSucceeds() throws Exception {
    VMProvisionCriteria criteria = new VMProvisionCriteria();
    criteria.setProjectId("proj1");
    criteria.setZone("zone1");
    criteria.setInstanceName("inst1");
    criteria.setTags("teamA");
    criteria.setMachineType("n1-standard-1");
    criteria.setDisks("disk1");

    Operation dummyOp = Operation.newBuilder().setName("op1").build();
    when(gcpProvisionService.provisionVM(any(VMProvisionCriteria.class)))
      .thenReturn(dummyOp);

    mockMvc.perform(post("/provisioning/vm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(criteria)))
      .andExpect(status().isOk())
      .andExpect(content().string("VM provisioning initiated successfully"));

    verify(gcpProvisionService).provisionVM(any(VMProvisionCriteria.class));
  }

  @Test
  void provisionVM_returnsInternalError_whenServiceThrows() throws Exception {
    VMProvisionCriteria criteria = new VMProvisionCriteria();
    criteria.setProjectId("proj1");

    when(gcpProvisionService.provisionVM(any(VMProvisionCriteria.class)))
      .thenThrow(new RuntimeException("provision failed"));

    mockMvc.perform(post("/provisioning/vm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(criteria)))
      .andExpect(status().isInternalServerError())
      .andExpect(content().string("Failed to provision VM: provision failed"));

    verify(gcpProvisionService).provisionVM(any(VMProvisionCriteria.class));
  }

  @Test
  void deleteVM_returnsOk_whenServiceSucceeds() throws Exception {
    Operation dummyOp = Operation.newBuilder().setName("delOp").build();
    when(gcpProvisionService.deleteVM("proj1", "zone1", "inst1"))
      .thenReturn(dummyOp);

    mockMvc.perform(delete("/provisioning/vm/zone1/inst1")
        .param("projectId", "proj1"))
      .andExpect(status().isOk())
      .andExpect(content().string("VM deletion initiated successfully"));

    verify(gcpProvisionService).deleteVM("proj1", "zone1", "inst1");
  }

  @Test
  void deleteVM_returnsInternalError_whenServiceThrows() throws Exception {
    when(gcpProvisionService.deleteVM("proj1", "zone1", "inst1"))
      .thenThrow(new RuntimeException("deletion failed"));

    mockMvc.perform(delete("/provisioning/vm/zone1/inst1")
        .param("projectId", "proj1"))
      .andExpect(status().isInternalServerError())
      .andExpect(content().string("Failed to delete VM: deletion failed"));

    verify(gcpProvisionService).deleteVM("proj1", "zone1", "inst1");
  }
}