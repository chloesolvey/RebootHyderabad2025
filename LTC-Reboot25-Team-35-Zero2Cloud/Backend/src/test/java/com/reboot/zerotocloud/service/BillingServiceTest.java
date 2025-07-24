package com.reboot.zerotocloud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.reboot.zerotocloud.model.BillingInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    private BillingService service;

    @BeforeEach
    void setUp() {
        service = new BillingService();
        // inject the private @Value field
        ReflectionTestUtils.setField(service, "billingTable", "proj.dataset.table");
    }

    @Test
    void fetchBillingData_successful() throws Exception {
        // 1) Static-mock BigQueryOptions.getDefaultInstance().getService()
        try (MockedStatic<BigQueryOptions> bqOpts = mockStatic(BigQueryOptions.class)) {
            BigQueryOptions opts = mock(BigQueryOptions.class);
            bqOpts.when(BigQueryOptions::getDefaultInstance).thenReturn(opts);

            BigQuery mockBigQuery = mock(BigQuery.class);
            when(opts.getService()).thenReturn(mockBigQuery);

            // 2) Prepare the three single-value results
            TableResult single1 = singleValueTableResult(100.0);
            TableResult single2 = singleValueTableResult(200.0);
            TableResult single3 = singleValueTableResult(50.0);

            // 3) Prepare the service-costs result
            TableResult svcCosts = mock(TableResult.class);
            FieldValueList rowA = mock(FieldValueList.class);
            FieldValue val= mock(FieldValue.class);
            when(rowA.get(0)).thenReturn(val);
            when(val.getStringValue()).thenReturn("Compute Engine");
            when(rowA.get(1)).thenReturn(val);
            when(val.getDoubleValue()).thenReturn(123.45);
            FieldValueList rowB = mock(FieldValueList.class);
            FieldValue valB1= mock(FieldValue.class);
            when(rowB.get(0)).thenReturn(valB1);
            when(valB1.getStringValue()).thenReturn("Cloud Storage");
            FieldValue valB2= mock(FieldValue.class);
            when(rowB.get(1)).thenReturn(valB2);
            when(valB2.getDoubleValue()).thenReturn(54.32);
            when(svcCosts.iterateAll()).thenReturn(List.of(rowA, rowB));

            // 4) Stub query(...) to return in order: current, previous, projected, serviceCosts
            when(mockBigQuery.query(any(QueryJobConfiguration.class)))
                .thenReturn(single1, single2, single3, svcCosts);

            // 5) Execute and verify
            BillingInfo info = service.fetchBillingData();

            assertEquals(100.0, info.getCurrentMonthSpend());
            assertEquals(200.0, info.getPreviousMonthSpend());
            assertEquals(50.0, info.getProjectedSpend());

            Map<String, Double> costs = info.getServiceWiseCosts();
            assertEquals(2, costs.size());
            assertEquals(123.45, costs.get("Compute Engine"));
            assertEquals(54.32, costs.get("Cloud Storage"));
        }
    }

    @Test
    void fetchBillingData_queryThrowsInterruptedException() throws Exception {
        try (MockedStatic<BigQueryOptions> bqOpts = mockStatic(BigQueryOptions.class)) {
            BigQueryOptions opts = mock(BigQueryOptions.class);
            bqOpts.when(BigQueryOptions::getDefaultInstance).thenReturn(opts);

            BigQuery mockBigQuery = mock(BigQuery.class);
            when(opts.getService()).thenReturn(mockBigQuery);

            // Any query(...) call now throws
            when(mockBigQuery.query(any(QueryJobConfiguration.class)))
                .thenThrow(new InterruptedException("query failed"));

            assertThrows(InterruptedException.class, () -> service.fetchBillingData());
        }
    }

    /**
     * Helper to create a one-row TableResult whose first column is a double.
     */
    private TableResult singleValueTableResult(double v) {
        TableResult tr = mock(TableResult.class);
        FieldValueList row = mock(FieldValueList.class);
        FieldValue fv = mock(FieldValue.class);

        when(fv.isNull()).thenReturn(false);
        when(fv.getDoubleValue()).thenReturn(v);
        when(row.get(0)).thenReturn(fv);
        when(tr.iterateAll()).thenReturn(List.of(row));

        return tr;
    }
}
