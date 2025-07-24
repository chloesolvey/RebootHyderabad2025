output "pubsub_topic" {
  value = google_pubsub_topic.bq_audit_logs.name
}

output "dataset" {
  value = google_bigquery_dataset.audit_logs.dataset_id
}
