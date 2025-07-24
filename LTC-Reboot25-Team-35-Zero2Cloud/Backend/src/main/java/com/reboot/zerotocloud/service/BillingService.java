package com.reboot.zerotocloud.service;

import com.google.cloud.bigquery.*;
import com.reboot.zerotocloud.model.BillingInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BillingService {

    @Value("${gcp.billing.table}")
    private String billingTable; // Format: project.dataset.table

    public BillingInfo fetchBillingData() throws InterruptedException {
        BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();

        double currentMonth = querySingleValue(bigQuery,
                "SELECT SUM(cost) FROM `" + billingTable + "` " +
                        "WHERE usage_start_time >= TIMESTAMP_TRUNC(CURRENT_TIMESTAMP(), MONTH)");

        double previousMonth = querySingleValue(bigQuery,
                "SELECT SUM(cost) FROM `" + billingTable + "` " +
                        "WHERE usage_start_time BETWEEN TIMESTAMP_SUB(TIMESTAMP_TRUNC(CURRENT_TIMESTAMP(), MONTH), INTERVAL 1 MONTH) " +
                        "AND TIMESTAMP_TRUNC(CURRENT_TIMESTAMP(), MONTH)");

        double projected = querySingleValue(bigQuery,
                "SELECT SUM(cost) / EXTRACT(DAY FROM CURRENT_DATE()) * 30 FROM `" + billingTable + "` " +
                        "WHERE usage_start_time >= TIMESTAMP_TRUNC(CURRENT_TIMESTAMP(), MONTH)");

        Map<String, Double> serviceCosts = new HashMap<>();
        String serviceQuery = "SELECT service.description, ROUND(SUM(cost), 2) FROM `" + billingTable + "` " +
                "WHERE usage_start_time >= TIMESTAMP_TRUNC(CURRENT_TIMESTAMP(), MONTH) " +
                "GROUP BY service.description ORDER BY 2 DESC";

        QueryJobConfiguration config = QueryJobConfiguration.newBuilder(serviceQuery).build();
        TableResult result = bigQuery.query(config);

        for (FieldValueList row : result.iterateAll()) {
            serviceCosts.put(row.get(0).getStringValue(), row.get(1).getDoubleValue());
        }

        return new BillingInfo(currentMonth, previousMonth, projected, serviceCosts);
    }

    private double querySingleValue(BigQuery bigQuery, String sql) throws InterruptedException {
        QueryJobConfiguration config = QueryJobConfiguration.newBuilder(sql).build();
        TableResult result = bigQuery.query(config);
        FieldValueList row = result.iterateAll().iterator().next();
        return row.get(0).isNull() ? 0.0 : row.get(0).getDoubleValue();
    }
}