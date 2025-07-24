SELECT
  forecast_timestamp,
  GREATEST(forecast_value, 0) AS forecast_recommendations,
  GREATEST(prediction_interval_lower_bound, 0) AS prediction_interval_lower,
  GREATEST(prediction_interval_upper_bound, 0) AS prediction_interval_upper
FROM
  ML.FORECAST(
    MODEL greenquery_core.forecast_recommendations,
    STRUCT(30 AS horizon, 0.9 AS confidence_level),
    TABLE greenquery_core.future_regressors_recommendations
  )
ORDER BY forecast_timestamp;
