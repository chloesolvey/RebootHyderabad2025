package com.reboot.zerotocloud.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.reboot.zerotocloud.dto.DataPoint;
import com.reboot.zerotocloud.dto.MonitoringRequest;
import com.reboot.zerotocloud.dto.MonitoringResponse;
import com.reboot.zerotocloud.model.Point;
import com.reboot.zerotocloud.model.TimeSeries;
import com.reboot.zerotocloud.model.TimeSeriesResponse;

@Service
public class GCPMonitoringService {

    private static final String BASE_URL = "https://monitoring.googleapis.com/v3/projects/{projectId}/timeSeries";

    private final GoogleCredentials credentials;
    private final RestTemplate restTemplate;
    private final String projectId;

    public GCPMonitoringService(GoogleCredentials credentials,
                                RestTemplate restTemplate,
                                @Value("${gcp.project-id}") String projectId) {
        this.credentials = credentials;
        this.restTemplate = restTemplate;
        this.projectId = projectId;
    }

    public MonitoringResponse getCpuUtilization(MonitoringRequest req) throws IOException {
        return fetchTimeSeries(
            "compute.googleapis.com/instance/cpu/utilization",
            "gce_instance",
            req,
            true
        );
    }

    public MonitoringResponse getMemoryUtilization(MonitoringRequest req) throws IOException {
        return fetchTimeSeries(
            "compute.googleapis.com/instance/memory/utilization",
            "gce_instance",
            req,
            true
        );
    }

    public MonitoringResponse getBucketBytes(MonitoringRequest req) throws IOException {
        return fetchTimeSeries(
            "storage.googleapis.com/storage/total_bytes",
            "gcs_bucket",
            req,
            false
        );
    }

    public MonitoringResponse getBucketObjectCount(MonitoringRequest req) throws IOException {
        return fetchTimeSeries(
            "storage.googleapis.com/storage/object_count",
            "gcs_bucket",
            req,
            false
        );
    }

    public MonitoringResponse getCloudSqlCpu(MonitoringRequest req) throws IOException {
        return fetchTimeSeries(
            "cloudsql.googleapis.com/database/cpu/utilization",
            "cloudsql_database",
            req,
            true
        );
    }

    public MonitoringResponse getCloudSqlConnections(MonitoringRequest req) throws IOException {
        return fetchTimeSeries(
            "cloudsql.googleapis.com/database/active_connections",
            "cloudsql_database",
            req,
            false
        );
    }

    private MonitoringResponse fetchTimeSeries(
        String metricType,
        String resourceType,
        MonitoringRequest req,
        boolean scaleToPercent
    ) throws IOException {
        // 1) Determine time window (UTC)
        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
        OffsetDateTime start;
        switch (req.getTimeframe()) {
            case LAST_HOUR:  start = end.minusHours(1);  break;
            case LAST_DAY:   start = end.minusDays(1);   break;
            case LAST_WEEK:  start = end.minusWeeks(1);  break;
            default:         start = req.getStartTime().atOffset(ZoneOffset.UTC);
                              end = req.getEndTime().atOffset(ZoneOffset.UTC);
        }

        // 2) Build URI
        String filter = String.format(
            "metric.type=\"%s\" AND resource.type=\"%s\"",
            metricType, resourceType
        );
        String uri = UriComponentsBuilder
            .fromHttpUrl(BASE_URL)
            .queryParam("filter", filter)
            .queryParam("interval.startTime", start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .queryParam("interval.endTime", end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .buildAndExpand(projectId)
            .toUriString();

     // 3) Grab a fresh access token
        String token = credentials
          .refreshAccessToken()
          .getTokenValue();

        // 4) Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 5) Create an HttpEntity (no body for GET)
        HttpEntity<Void> entity = new HttpEntity<>(headers);

     // 6) Exchange
        ResponseEntity<TimeSeriesResponse> resp = restTemplate.exchange(
          uri,
          HttpMethod.GET,
          entity,
          TimeSeriesResponse.class
        );

        TimeSeriesResponse response = resp.getBody();


//        // 3) Retrieve access token
//        String token = credentials.refreshAccessToken().getTokenValue();

//        // 4) Call GCP Monitoring REST API
//        var response = restTemplate.getForEntity(
//            uri,
//            TimeSeriesResponse.class,
//            new org.springframework.http.HttpHeaders() {{
//                setBearerAuth(token);
//            }}
//        ).getBody();

        // 5) Flatten into DataPoint list
        List<DataPoint> points = new ArrayList<>();
        if (response != null && response.timeSeries() != null) {
            for (TimeSeries ts : response.timeSeries()) {
                for (Point p : ts.points()) {
                    double value = p.value().doubleValue() != null
                                 ? p.value().doubleValue()
                                 : p.value().int64Value();

                    double finalVal = scaleToPercent ? value * 100.0 : value;
                    OffsetDateTime tsStamp = OffsetDateTime.parse(p.interval().endTime());
                    points.add(new DataPoint(tsStamp, finalVal));
                }
            }
        }

        MonitoringResponse mr = new MonitoringResponse();
        mr.setPoints(points);
        return mr;
    }
}
