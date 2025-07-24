SELECT
  protopayload_auditlog.authenticationInfo.principalEmail AS user_email,
  protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobName.jobId AS job_id,
  protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.eventName AS raw_job_type,
  CASE 
    WHEN protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.eventName = 'query_job_completed' THEN 'QUERY'
    WHEN protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.eventName LIKE '%insert%' THEN 'INSERT'
    WHEN protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.eventName LIKE '%create%' THEN 'CREATE'
    WHEN protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.eventName LIKE '%update%' THEN 'UPDATE'
    ELSE 'OTHER'
  END AS job_type,
  DATETIME(protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.createTime, "Asia/Kolkata") AS job_run_time_ist,
  FORMAT_DATETIME("%Y-%m-%d %H:%M:%S", DATETIME(protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.createTime, "Asia/Kolkata")) AS job_run_time_ist_str,
  TIMESTAMP_DIFF(
    protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.endTime,
    protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.createTime,
    MINUTE
  ) AS runtime_minutes,
  SAFE_DIVIDE(
    protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.totalSlotMs,
    GREATEST(
      TIMESTAMP_DIFF(
        protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.endTime,
        protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.createTime,
        MILLISECOND
      ),
      1
    )
  ) AS avg_slot_usage,
  ROUND(
    protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.totalProcessedBytes / (1024*1024),
    2
  ) AS processed_mb
FROM
  `audit_logs.cloudaudit_googleapis_com_data_access`
WHERE
  protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.eventName IS NOT NULL
  AND protopayload_auditlog.authenticationInfo.principalEmail IS NOT NULL
  AND protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobStatistics.createTime 
      >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 30 DAY)
GROUP BY
  user_email,
  job_id,
  raw_job_type,
  job_type,
  job_run_time_ist,
  job_run_time_ist_str,
  runtime_minutes,
  avg_slot_usage,
  processed_mb
ORDER BY
  job_run_time_ist DESC;