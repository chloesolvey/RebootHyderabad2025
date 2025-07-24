from google.cloud import bigquery

# Initialize BigQuery client at module level for reuse
client = bigquery.Client()

def run_dry_run(sql_text):
    """
    Execute a dry run of the given SQL query in BigQuery to estimate bytes processed and cost without running it.
    
    Args:
        sql_text (str): SQL query string
        
    Returns:
        tuple: (bytes_processed (int), estimated_cost_usd (float)) or (None, None) if dry run fails
    """
    try:
        job_config = bigquery.QueryJobConfig(dry_run=True, use_query_cache=False)
        query_job = client.query(sql_text, job_config=job_config)
        bytes_processed = query_job.total_bytes_processed
        cost_usd = (bytes_processed / (1024**4)) * 5  # $5 per TB pricing assumption
        return bytes_processed, cost_usd
    except Exception:
        return None, None

def create_bq_table_if_not_exists(dataset_id, table_id):
    """
    Check if a BigQuery table exists; if not, create it with appropriate schema and partitioning.
    
    Args:
        dataset_id (str): BigQuery dataset name
        table_id (str): BigQuery table name
    """
    dataset_ref = client.dataset(dataset_id)
    table_ref = dataset_ref.table(table_id)

    schema = [
        bigquery.SchemaField("record_hash", "STRING", mode="REQUIRED"),
        bigquery.SchemaField("file", "STRING"),
        bigquery.SchemaField("processed_gb", "FLOAT"),
        bigquery.SchemaField("estimated_cost_usd", "FLOAT"),
        bigquery.SchemaField("estimated_carbon_kg", "FLOAT"),
        bigquery.SchemaField("recommendations", "STRING", mode="REPEATED"),
        bigquery.SchemaField("load_time_utc", "TIMESTAMP"),
    ]

    table = bigquery.Table(table_ref, schema=schema)
    # Partition by day on load_time_utc timestamp
    table.time_partitioning = bigquery.TimePartitioning(
        type_=bigquery.TimePartitioningType.DAY,
        field="load_time_utc"
    )

    try:
        client.get_table(table_ref)  # If table exists, do nothing
        print(f"Table {dataset_id}.{table_id} exists.")
    except Exception:
        client.create_table(table)  # Create if not exists
        print(f"Created table {dataset_id}.{table_id}.")

def merge_staging_into_target(dataset_id, target_table_id, staging_table_id):
    """
    Performs a BigQuery MERGE statement from a staging table into the main target table,
    updating only new/distinct rows based on 'record_hash'.
    
    Args:
        dataset_id (str): BigQuery dataset name
        target_table_id (str): Main table name
        staging_table_id (str): Staging table name
    """
    merge_sql = f"""
    MERGE `{dataset_id}.{target_table_id}` T
    USING `{dataset_id}.{staging_table_id}` S
    ON T.record_hash = S.record_hash
    WHEN NOT MATCHED THEN
      INSERT (record_hash, file, processed_gb, estimated_cost_usd,
              estimated_carbon_kg, recommendations, load_time_utc)
      VALUES (S.record_hash, S.file, S.processed_gb, S.estimated_cost_usd,
              S.estimated_carbon_kg, S.recommendations, S.load_time_utc)
    """
    query_job = client.query(merge_sql)
    query_job.result()
    print(f"Merged staging table {staging_table_id} into {target_table_id}.")

def load_dataframe_to_bq_upsert(df, dataset_id, target_table_id):
    """
    Load a pandas DataFrame to a BigQuery staging table and upsert it into the main target table,
    avoiding duplicate records using record_hash deduplication.
    
    Args:
        df (pandas.DataFrame): Data to load
        dataset_id (str): Dataset name
        target_table_id (str): Target table name
    """
    staging_table_id = target_table_id + "_staging"
    dataset_ref = client.dataset(dataset_id)
    staging_table_ref = dataset_ref.table(staging_table_id)

    schema = [
        bigquery.SchemaField("record_hash", "STRING", mode="REQUIRED"),
        bigquery.SchemaField("file", "STRING"),
        bigquery.SchemaField("processed_gb", "FLOAT"),
        bigquery.SchemaField("estimated_cost_usd", "FLOAT"),
        bigquery.SchemaField("estimated_carbon_kg", "FLOAT"),
        bigquery.SchemaField("recommendations", "STRING", mode="REPEATED"),
        bigquery.SchemaField("load_time_utc", "TIMESTAMP"),
    ]

    try:
        client.get_table(staging_table_ref)
        # Truncate staging table before load
        client.query(f"TRUNCATE TABLE `{dataset_id}.{staging_table_id}`").result()
        print(f"Truncated staging table {staging_table_id}")
    except Exception:
        # Create staging table if it doesn't exist
        table = bigquery.Table(staging_table_ref, schema=schema)
        client.create_table(table)
        print(f"Created staging table {staging_table_id}")

    load_job_config = bigquery.LoadJobConfig(
        write_disposition=bigquery.WriteDisposition.WRITE_APPEND,
    )

    load_job = client.load_table_from_dataframe(df, staging_table_ref, job_config=load_job_config)
    load_job.result()
    print(f"Loaded {load_job.output_rows} rows into staging table {staging_table_id}")

    # Merge staging into main table without duplications
    merge_staging_into_target(dataset_id, target_table_id, staging_table_id)
