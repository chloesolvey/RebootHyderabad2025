package com.reboot.zerotocloud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class GCPLabelServiceTest {

    private static final String PROJECT_ID = "test-project";
    private static final String TEAM_LABEL_KEY = "team";
    private static final String LABEL_VALUE = "teamA";

    @Test
    void addTeamLabel_success() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            // Arrange static ResourceManagerOptions and ResourceManager
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            // Arrange existing project with no labels
            Project project = mock(Project.class);
            when(rm.get(PROJECT_ID)).thenReturn(project);
            when(project.getLabels()).thenReturn(null);

            Project.Builder builder = mock(Project.Builder.class);
            when(project.toBuilder()).thenReturn(builder);
            when(builder.setLabels(anyMap())).thenReturn(builder);
            when(builder.build()).thenReturn(project);

            when(rm.replace(project)).thenReturn(project);

            GCPLabelService service = new GCPLabelService();

            // Act
            boolean result = service.addTeamLabel(PROJECT_ID, LABEL_VALUE);

            // Assert
            assertTrue(result);
            Map<String, String> expectedLabels = new HashMap<>();
            expectedLabels.put(TEAM_LABEL_KEY, LABEL_VALUE);
            verify(builder).setLabels(expectedLabels);
            verify(rm).replace(project);
        }
    }

    @Test
    void addTeamLabel_alreadyExists() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            Project project = mock(Project.class);
            Map<String, String> labels = Collections.singletonMap(TEAM_LABEL_KEY, "existing");
            when(rm.get(PROJECT_ID)).thenReturn(project);
            when(project.getLabels()).thenReturn(labels);

            GCPLabelService service = new GCPLabelService();

            boolean result = service.addTeamLabel(PROJECT_ID, LABEL_VALUE);

            assertFalse(result);
            verify(project, never()).toBuilder();
            verify(rm, never()).replace(any());
        }
    }

    @Test
    void addTeamLabel_projectNotFound_throws() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            when(rm.get(PROJECT_ID)).thenReturn(null);
            GCPLabelService service = new GCPLabelService();

            RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addTeamLabel(PROJECT_ID, LABEL_VALUE));

            assertTrue(ex.getMessage().contains("Project not found"));
        }
    }

    @Test
    void addTeamLabel_replaceReturnsNull_returnsFalse() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            Project project = mock(Project.class);
            when(rm.get(PROJECT_ID)).thenReturn(project);
            when(project.getLabels()).thenReturn(Collections.emptyMap());

            Project.Builder builder = mock(Project.Builder.class);
            when(project.toBuilder()).thenReturn(builder);
            when(builder.setLabels(anyMap())).thenReturn(builder);
            when(builder.build()).thenReturn(project);

            when(rm.replace(project)).thenReturn(null);

            GCPLabelService service = new GCPLabelService();
            boolean result = service.addTeamLabel(PROJECT_ID, LABEL_VALUE);

            assertFalse(result);
        }
    }

    @Test
    void getProjectLabels_success() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            Map<String, String> labels = Collections.singletonMap(TEAM_LABEL_KEY, LABEL_VALUE);
            Project project = mock(Project.class);
            when(rm.get(PROJECT_ID)).thenReturn(project);
            when(project.getLabels()).thenReturn(labels);

            GCPLabelService service = new GCPLabelService();
            Map<String, String> result = service.getProjectLabels(PROJECT_ID);

            assertEquals(labels, result);
        }
    }

    @Test
    void getProjectLabels_projectNotFound_throws() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            when(rm.get(PROJECT_ID)).thenReturn(null);
            GCPLabelService service = new GCPLabelService();

            RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getProjectLabels(PROJECT_ID));

            assertTrue(ex.getMessage().contains("Project not found"));
        }
    }

    @Test
    void removeTeamLabel_success() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            Map<String, String> labels = new HashMap<>();
            labels.put(TEAM_LABEL_KEY, LABEL_VALUE);
            Project project = mock(Project.class);
            when(rm.get(PROJECT_ID)).thenReturn(project);
            when(project.getLabels()).thenReturn(labels);

            Project.Builder builder = mock(Project.Builder.class);
            when(project.toBuilder()).thenReturn(builder);
            when(builder.setLabels(anyMap())).thenReturn(builder);
            when(builder.build()).thenReturn(project);

            when(rm.replace(project)).thenReturn(project);

            GCPLabelService service = new GCPLabelService();
            boolean result = service.removeTeamLabel(PROJECT_ID);

            assertTrue(result);
            Map<String, String> expected = new HashMap<>();
            verify(builder).setLabels(expected);
            verify(rm).replace(project);
        }
    }

    @Test
    void removeTeamLabel_noTeam_returnsFalse() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            Project project = mock(Project.class);
            when(rm.get(PROJECT_ID)).thenReturn(project);
            when(project.getLabels()).thenReturn(Collections.emptyMap());

            GCPLabelService service = new GCPLabelService();
            assertFalse(service.removeTeamLabel(PROJECT_ID));
            verify(project, never()).toBuilder();
            verify(rm, never()).replace(any());
        }
    }

    @Test
    void removeTeamLabel_projectNotFound_throws() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            when(rm.get(PROJECT_ID)).thenReturn(null);
            GCPLabelService service = new GCPLabelService();

            RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.removeTeamLabel(PROJECT_ID));
            assertTrue(ex.getMessage().contains("Project not found"));
        }
    }

    @Test
    void removeTeamLabel_replaceReturnsNull_returnsFalse() {
        try (MockedStatic<ResourceManagerOptions> rmOpts = mockStatic(ResourceManagerOptions.class)) {
            ResourceManagerOptions opts = mock(ResourceManagerOptions.class);
            rmOpts.when(ResourceManagerOptions::getDefaultInstance).thenReturn(opts);
            ResourceManager rm = mock(ResourceManager.class);
            when(opts.getService()).thenReturn(rm);

            Map<String, String> labels = new HashMap<>();
            labels.put(TEAM_LABEL_KEY, LABEL_VALUE);
            Project project = mock(Project.class);
            when(rm.get(PROJECT_ID)).thenReturn(project);
            when(project.getLabels()).thenReturn(labels);

            Project.Builder builder = mock(Project.Builder.class);
            when(project.toBuilder()).thenReturn(builder);
            when(builder.setLabels(anyMap())).thenReturn(builder);
            when(builder.build()).thenReturn(project);

            when(rm.replace(project)).thenReturn(null);

            GCPLabelService service = new GCPLabelService();
            assertFalse(service.removeTeamLabel(PROJECT_ID));
        }
    }
}
