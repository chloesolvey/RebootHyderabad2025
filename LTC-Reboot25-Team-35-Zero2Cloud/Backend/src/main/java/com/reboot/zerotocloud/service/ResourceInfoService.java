package com.reboot.zerotocloud.service;

import com.google.cloud.compute.v1.*;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.monitoring.v3.*;
import com.google.protobuf.util.Timestamps;
import com.reboot.zerotocloud.model.GcpVmInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class ResourceInfoService {

    @Value("${gcp.project-id}")
    private String projectId;

    public ResponseEntity<List<GcpVmInfo>> getAllVmInfo() throws IOException {
        List<GcpVmInfo> result = new ArrayList<>();

        try (InstancesClient instancesClient = InstancesClient.create();
             DisksClient disksClient = DisksClient.create();
             MetricServiceClient metricClient = MetricServiceClient.create()) {

            AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                    .setProject(projectId)
                    .build();

            for (Map.Entry<String, InstancesScopedList> entry : instancesClient.aggregatedList(request).iterateAll()) {
                if (entry.getValue().getInstancesList() != null) {
                    for (Instance instance : entry.getValue().getInstancesList()) {

                        String zone = extractLast(instance.getZone());
                        String machineType = extractLast(instance.getMachineType());
                        String instanceName = instance.getName();
                        String createdTime = instance.getCreationTimestamp();
                        String status = instance.getStatus();
                        long instanceId = instance.getId();

                        String sourceImage = "unknown";

                        try {
                            String diskUrl = instance.getDisks(0).getSource(); // full path
                            String diskName = extractLast(diskUrl);

                            Disk disk = disksClient.get(projectId, zone, diskName);
                            if (disk.hasSourceImage()) {
                                sourceImage = disk.getSourceImage(); // full image path
                            }
                        } catch (Exception e) {
                            sourceImage = "unavailable";
                        }

                        String memoryUsage = "N/A";
                        try {
                            double mem = getMemoryUsage(metricClient, projectId, instanceId);
                            memoryUsage = String.format("%.2f", mem);
                        } catch (Exception e) {
                            memoryUsage = "N/A";
                        }

                        GcpVmInfo vmInfo = new GcpVmInfo(
                                instanceName,
                                status,
                                machineType,
                                zone,
                                sourceImage,
                                createdTime,
                                memoryUsage
                        );

                        result.add(vmInfo);
                    }
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    private String extractLast(String fullPath) {
        return fullPath.substring(fullPath.lastIndexOf("/") + 1);
    }

    private double getMemoryUsage(MetricServiceClient metricClient, String projectId, long instanceId) throws IOException {
        TimeInterval interval = TimeInterval.newBuilder()
                .setStartTime(Timestamps.fromMillis(System.currentTimeMillis() - 5 * 60 * 1000))
                .setEndTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        String filter = String.format(
                "metric.type=\"agent.googleapis.com/memory/percent_used\" AND resource.labels.instance_id=\"%s\"",
                instanceId);

        ListTimeSeriesRequest request = ListTimeSeriesRequest.newBuilder()
                .setName(ProjectName.of(projectId).toString())
                .setFilter(filter)
                .setInterval(interval)
                .setView(ListTimeSeriesRequest.TimeSeriesView.FULL)
                .build();

        MetricServiceClient.ListTimeSeriesPagedResponse response = metricClient.listTimeSeries(request);

        return StreamSupport.stream(response.iterateAll().spliterator(), false)
                .flatMap(ts -> ts.getPointsList().stream())
                .mapToDouble(p -> p.getValue().getDoubleValue())
                .average()
                .orElse(0.0);
    }

}
