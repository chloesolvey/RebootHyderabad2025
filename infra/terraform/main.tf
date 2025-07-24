provider "google" {
  project = var.project_id
  region  = var.region
}

resource "google_pubsub_topic" "bq_audit_logs" {
  name = "greenquery-audit-logs-topic"
}

resource "google_bigquery_dataset" "audit_logs" {
  dataset_id = "greenquery_logs"
  location   = var.region
}

resource "google_bigquery_table" "audit_logs" {
  dataset_id = google_bigquery_dataset.audit_logs.dataset_id
  table_id   = "audit_logs"
  schema     = file("${path.module}/schema/audit_logs_schema.json")
}

resource "google_logging_project_sink" "bq_sink" {
  name        = "greenquery-bq-log-sink"
  destination = "pubsub.googleapis.com/projects/${var.project_id}/topics/${google_pubsub_topic.bq_audit_logs.name}"
  filter      = "resource.type=\"bigquery_resource\""
  unique_writer_identity = true
}
