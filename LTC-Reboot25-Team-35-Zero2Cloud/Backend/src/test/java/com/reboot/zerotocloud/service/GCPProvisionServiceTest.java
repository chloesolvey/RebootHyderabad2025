package com.reboot.zerotocloud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.api.gax.rpc.NotFoundException;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.DeleteInstanceRequest;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.reboot.zerotocloud.model.VMProvisionCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class GCPProvisionServiceTest {

    private static final String PROJECT_ID = "proj-123";
    private static final String ZONE       = "us-central1-a";
    private static final String INSTANCE   = "my-instance";
    private static final String MACHINE    = "n1-standard-1";
    private static final String TEAM_LABEL = "teamA";

    @Mock
    private GCPLabelService gcpLabelService;

    private GCPProvisionService service;

    @BeforeEach
    void setUp() {
        service = new GCPProvisionService();
        ReflectionTestUtils.setField(service, "gcpLabelService", gcpLabelService);
    }

    private VMProvisionCriteria buildCriteria(String tag) {
        VMProvisionCriteria crit = new VMProvisionCriteria();
        crit.setProjectId(PROJECT_ID);
        crit.setZone(ZONE);
        crit.setInstanceName(INSTANCE);
        crit.setMachineType(MACHINE);
        crit.setTags(tag);
        // disks property is not used in this service
        return crit;
    }

    @Test
    void provisionVM_noTeamLabel_shouldThrow() {
        VMProvisionCriteria crit = buildCriteria("any");
        when(gcpLabelService.getProjectLabels(PROJECT_ID)).thenReturn(Map.of());

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.provisionVM(crit)
        );
        assertTrue(ex.getMessage().contains("No team label found for project"));
    }

    @Test
    void provisionVM_tagMismatch_shouldThrow() {
        VMProvisionCriteria crit = buildCriteria("wrongTag");
        when(gcpLabelService.getProjectLabels(PROJECT_ID))
            .thenReturn(Map.of("team", TEAM_LABEL));

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.provisionVM(crit)
        );
        assertTrue(ex.getMessage()
            .contains("does not match the registered team label"));
    }

    @Test
    void provisionVM_success_shouldReturnOperation()
        throws IOException, InterruptedException, ExecutionException 
    {
        VMProvisionCriteria crit = buildCriteria(TEAM_LABEL);
        when(gcpLabelService.getProjectLabels(PROJECT_ID))
            .thenReturn(Map.of("team", TEAM_LABEL));

        try (MockedStatic<InstancesClient> instClientStatic = mockStatic(InstancesClient.class)) {
            InstancesClient mockClient = mock(InstancesClient.class);
            instClientStatic.when(InstancesClient::create).thenReturn(mockClient);

            @SuppressWarnings("unchecked")
            OperationFuture<Operation, Operation> fakeFuture =
                (OperationFuture<Operation, Operation>) mock(OperationFuture.class);

            Operation fakeOp = Operation.newBuilder().setName("op123").build();
            when(mockClient.insertAsync(any(InsertInstanceRequest.class)))
                .thenReturn(fakeFuture);
            when(fakeFuture.get()).thenReturn(fakeOp);

            Operation result = service.provisionVM(crit);
            assertNotNull(result);
            assertEquals("op123", result.getName());

            verify(mockClient).insertAsync(argThat(req ->
                PROJECT_ID.equals(req.getProject()) &&
                ZONE.equals(req.getZone()) &&
                INSTANCE.equals(req.getInstanceResource().getName()) &&
                TEAM_LABEL.equals(req.getInstanceResource()
                    .getLabelsMap().get("team"))
            ));
        }
    }

    @Test
    void deleteVM_success_shouldReturnOperation()
        throws IOException, InterruptedException, ExecutionException 
    {
        when(gcpLabelService.getProjectLabels(PROJECT_ID))
            .thenReturn(Map.of("team", TEAM_LABEL));

        try (MockedStatic<InstancesClient> instClientStatic = mockStatic(InstancesClient.class)) {
            InstancesClient mockClient = mock(InstancesClient.class);
            instClientStatic.when(InstancesClient::create).thenReturn(mockClient);

            Instance vm = Instance.newBuilder()
                .putLabels("team", TEAM_LABEL)
                .build();
            when(mockClient.get(PROJECT_ID, ZONE, INSTANCE)).thenReturn(vm);

            @SuppressWarnings("unchecked")
            OperationFuture<Operation, Operation> fakeDelFuture =
                (OperationFuture<Operation, Operation>) mock(OperationFuture.class);
            Operation delOp = Operation.newBuilder().setName("delOp").build();

            when(mockClient.deleteAsync(any(DeleteInstanceRequest.class)))
                .thenReturn(fakeDelFuture);
            when(fakeDelFuture.get()).thenReturn(delOp);

            Operation result = service.deleteVM(PROJECT_ID, ZONE, INSTANCE);
            assertNotNull(result);
            assertEquals("delOp", result.getName());

            verify(mockClient).deleteAsync(argThat(req ->
                PROJECT_ID.equals(req.getProject()) &&
                ZONE.equals(req.getZone()) &&
                INSTANCE.equals(req.getInstance())
            ));
        }
    }

    @Test
    void deleteVM_notFound_shouldThrow() {
//        when(gcpLabelService.getProjectLabels(PROJECT_ID))
//            .thenReturn(Map.of("team", TEAM_LABEL));

        try (MockedStatic<InstancesClient> instClientStatic = mockStatic(InstancesClient.class)) {
            InstancesClient mockClient = mock(InstancesClient.class);
            instClientStatic.when(InstancesClient::create).thenReturn(mockClient);

            when(mockClient.get(PROJECT_ID, ZONE, INSTANCE))
                .thenThrow(NotFoundException.class);

            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.deleteVM(PROJECT_ID, ZONE, INSTANCE)
            );
            assertTrue(ex.getMessage().contains("VM not found"));
        }
    }

    @Test
    void deleteVM_labelMismatch_shouldThrow() throws IOException {
        when(gcpLabelService.getProjectLabels(PROJECT_ID))
            .thenReturn(Map.of("team", TEAM_LABEL));

        try (MockedStatic<InstancesClient> instClientStatic = mockStatic(InstancesClient.class)) {
            InstancesClient mockClient = mock(InstancesClient.class);
            instClientStatic.when(InstancesClient::create).thenReturn(mockClient);

            Instance vm = Instance.newBuilder()
                .putLabels("team", "otherTeam")
                .build();
            when(mockClient.get(PROJECT_ID, ZONE, INSTANCE)).thenReturn(vm);

            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.deleteVM(PROJECT_ID, ZONE, INSTANCE)
            );
            assertTrue(ex.getMessage()
                .contains("Instance team label does not match project"));
        }
    }
}