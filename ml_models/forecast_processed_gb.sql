SELECT
  forecast_timestamp,
  GREATEST(forecast_value, 0) AS forecast_processed_gb,
  GREATEST(prediction_interval_lower_bound, 0) AS prediction_interval_lower,
  GREATEST(prediction_interval_upper_bound, 0) AS prediction_interval_upper
FROM
  ML.FORECAST(
    MODEL greenquery_core.forecast_processed_gb,
    STRUCT(30 AS horizon, 0.9 AS confidence_level),
    TABLE greenquery_core.future_regressors_processed_gb
  )
ORDER BY forecast_timestamp;
