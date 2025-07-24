package com.reboot.zerotocloud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.reboot.zerotocloud.dto.DataPoint;
import com.reboot.zerotocloud.dto.MonitoringRequest;
import com.reboot.zerotocloud.dto.MonitoringResponse;
import com.reboot.zerotocloud.dto.TimeframeType;
import com.reboot.zerotocloud.model.Point;
import com.reboot.zerotocloud.model.TimeSeries;
import com.reboot.zerotocloud.model.TimeSeriesResponse;

@ExtendWith(MockitoExtension.class)
class GCPMonitoringServiceTest {

    @Mock
    private GoogleCredentials credentials;

    @Mock
    private RestTemplate restTemplate;

    private GCPMonitoringService service;

    private static final String PROJECT_ID = "dummy-project";

    @BeforeEach
    void setup() throws IOException {
        // Inject mocks into our service
        service = new GCPMonitoringService(credentials, restTemplate, PROJECT_ID);

        // By default, return a dummy access token
        AccessToken dummyToken = new AccessToken("fake-token", null);
        when(credentials.refreshAccessToken()).thenReturn(dummyToken);
    }

    @Test
    void getCpuUtilization_shouldScaleDoubleValueBy100() throws IOException {
        // Prepare a LAST_HOUR request
        MonitoringRequest req = new MonitoringRequest();
        req.setTimeframe(TimeframeType.LAST_HOUR);

        // Build a fake TimeSeriesResponse with one TimeSeries → one Point
        TimeSeriesResponse tsResponse = mock(TimeSeriesResponse.class);
        TimeSeries ts = mock(TimeSeries.class);
        // Deep stub Point so we can chain .value().doubleValue() and .interval().endTime()
        Point p = mock(Point.class, RETURNS_DEEP_STUBS);

        String isoStamp = "2025-07-24T10:15:30Z";

        // Stub value and timestamp
        when(p.value().doubleValue()).thenReturn(0.5);
        when(p.interval().endTime()).thenReturn(isoStamp);

        when(ts.points()).thenReturn(List.of(p));
        when(tsResponse.timeSeries()).thenReturn(List.of(ts));

        // Stub RestTemplate.exchange(...) to return our fake response
        ResponseEntity<TimeSeriesResponse> fakeResp =
            new ResponseEntity<>(tsResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TimeSeriesResponse.class)
            ))
        .thenReturn(fakeResp);

        // Execute
        MonitoringResponse result = service.getCpuUtilization(req);
        List<DataPoint> points = result.getPoints();

        // Verify
        assertEquals(1, points.size());
        DataPoint dp = points.get(0);
        assertEquals(OffsetDateTime.parse(isoStamp), dp.getTimestamp());
        // 0.5 * 100 => 50.0
        assertEquals(50.0, dp.getValue());
    }

    @Test
    void getBucketBytes_shouldUseInt64ValueWhenDoubleIsNull_andNoScaling() throws IOException {
        MonitoringRequest req = new MonitoringRequest();
        req.setTimeframe(TimeframeType.LAST_HOUR);

        TimeSeriesResponse tsResponse = mock(TimeSeriesResponse.class);
        TimeSeries ts = mock(TimeSeries.class);
        Point p = mock(Point.class, RETURNS_DEEP_STUBS);

        String isoStamp = "2025-07-24T12:00:00Z";

        // Force doubleValue() == null so it falls back to int64Value()
        when(p.value().doubleValue()).thenReturn(null);
        when(p.value().int64Value()).thenReturn(42L);
        when(p.interval().endTime()).thenReturn(isoStamp);

        when(ts.points()).thenReturn(List.of(p));
        when(tsResponse.timeSeries()).thenReturn(List.of(ts));

        ResponseEntity<TimeSeriesResponse> fakeResp =
            new ResponseEntity<>(tsResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TimeSeriesResponse.class)
            ))
        .thenReturn(fakeResp);

        MonitoringResponse result = service.getBucketBytes(req);
        List<DataPoint> points = result.getPoints();

        assertEquals(1, points.size());
        DataPoint dp = points.get(0);
        assertEquals(OffsetDateTime.parse(isoStamp), dp.getTimestamp());
        // int64Value of 42 → 42.0 (no scaling)
        assertEquals(42.0, dp.getValue());
    }

    @Test
    void getMemoryUtilization_whenNoTimeSeries_returnsEmptyPointsList() throws IOException {
        MonitoringRequest req = new MonitoringRequest();
        req.setTimeframe(TimeframeType.LAST_DAY);

        TimeSeriesResponse tsResponse = mock(TimeSeriesResponse.class);
        // Simulate null timeSeries() → empty result
        when(tsResponse.timeSeries()).thenReturn(null);

        ResponseEntity<TimeSeriesResponse> fakeResp =
            new ResponseEntity<>(tsResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TimeSeriesResponse.class)
            ))
        .thenReturn(fakeResp);

        MonitoringResponse result = service.getMemoryUtilization(req);
        assertNotNull(result.getPoints());
        assertTrue(result.getPoints().isEmpty());
    }

    @Test
    void getCpuUtilization_shouldPropagateIOExceptionFromCredentials() throws IOException {
        // Simulate a failure obtaining the access token
        when(credentials.refreshAccessToken())
            .thenThrow(new IOException("unable to get token"));

        MonitoringRequest req = new MonitoringRequest();
        req.setTimeframe(TimeframeType.LAST_WEEK);

        // Expect the original IOException to bubble up
        assertThrows(IOException.class, () -> service.getCpuUtilization(req));
    }
}
