import os
from airflow import DAG
from airflow.providers.google.cloud.operators.bigquery import BigQueryCreateEmptyTableOperator, BigQueryInsertJobOperator
from airflow.providers.google.cloud.transfers.gcs_to_bigquery import GCSToBigQueryOperator
from airflow.utils.dates import days_ago

# Define your Google Cloud Project ID and Dataset ID
PROJECT_ID = "iconic-iridium-463506-v6"
DATASET_ID = "cloudcompass_scd2_composer"

# Define your GCS bucket and file paths
GCS_BUCKET = " carboncompass-inbound"
TRANSACTIONS_CSV_PATH = "data/transactions.csv"
SQL_FILE_PATH = "scripts/scd2_load.sql"  # Path to your SQL file within the DAGs folder in Composer's GCS bucket

# Default arguments for the DAG
default_args = {
    'owner': 'airflow',
    'start_date': days_ago(1),
    'depends_on_past': False,
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
}

with DAG(
    dag_id='bigquery_data_pipeline',
    default_args=default_args,
    description='A DAG to load data from GCS to BigQuery and execute a SQL file.',
    schedule_interval=None,  # Set your desired schedule interval (e.g., '@daily', timedelta(days=1))
    catchup=False,
    tags=['bigquery', 'gcs', 'composer'],
) as dag:

    # Task 1: Load data from GCS CSV to BigQuery 'transactions' table
    # This task assumes your 'transactions' table structure matches the CSV.
    # If the table doesn't exist, it will be created based on the schema.
    # load_transactions_from_gcs = GCSToBigQueryOperator(
    #     task_id='load_transactions_from_gcs',
    #     bucket=GCS_BUCKET,
    #     source_objects=[TRANSACTIONS_CSV_PATH],
    #     destination_project_dataset_table=f"{PROJECT_ID}.{DATASET_ID}.transactions",
    #     schema_fields=[
    #         {'name': 'transaction_id', 'type': 'STRING', 'mode': 'REQUIRED'},
    #         {'name': 'user_id', 'type': 'STRING', 'mode': 'REQUIRED'},
    #         {'name': 'transaction_date', 'type': 'DATE', 'mode': 'REQUIRED'},
    #         {'name': 'transaction_description', 'type': 'STRING', 'mode': 'REQUIRED'},
    #         {'name': 'transaction_amount', 'type': 'NUMERIC', 'mode': 'REQUIRED'},
    #         {'name': 'currency', 'type': 'STRING', 'mode': 'REQUIRED'},
    #         {'name': 'ingestion_timestamp', 'type': 'TIMESTAMP', 'mode': 'NULLABLE'},
    #     ],
    #     source_format='CSV',
    #     skip_leading_rows=1,  # Skip header row if your CSV has one
    #     create_disposition='CREATE_IF_NEEDED',
    #     write_disposition='WRITE_APPEND', # or 'WRITE_TRUNCATE' if you want to overwrite daily
    #     autodetect=False, # Set to True if you want BigQuery to infer schema
    # )

    # Task 2: Execute the provided BigQuery SQL file
    # This task will run all queries in your scd2_load.sql file.
    # Ensure the SQL file contains valid DDL (CREATE TABLE) and DML (INSERT) statements.
    execute_sql_file = BigQueryInsertJobOperator(
        task_id='execute_scd2_load_sql',
        configuration={
            "query": {
                "query": "{% include 'scd2_load.sql' %}",
                "useLegacySql": False,
                "priority": "BATCH",
            }
        },
        # The 'project_id' and 'location' are typically inferred from the Composer environment.
        # You can explicitly set them if needed:
        project_id=PROJECT_ID,
        location='europe-west2', # Ensure this matches your BigQuery dataset location
    )

    # Define the task dependencies
    # load_transactions_from_gcs >> execute_sql_file
    execute_sql_file