# bq_column_resolver.py

from google.cloud import bigquery

def get_table_columns(project_id: str, dataset_id: str, table_id: str) -> list:
    """
    Fetches the list of column names for a BigQuery table.

    Args:
        project_id (str): GCP project ID
        dataset_id (str): BigQuery dataset ID
        table_id (str): BigQuery table ID

    Returns:
        List[str]: List of column names
    """
    client = bigquery.Client(project=project_id)
    table_ref = f"{project_id}.{dataset_id}.{table_id}"

    try:
        table = client.get_table(table_ref)
        column_names = [field.name for field in table.schema]
        return column_names
    except Exception as e:
        print(f"❌ Failed to fetch schema for {table_ref}: {e}")
        return []

# Example usage
if __name__ == "__main__":
    project = "chrome-inkwell-466604-r4"
    dataset = "core_data"
    table = "transaction_details"

    columns = get_table_columns(project, dataset, table)
    print(f"✅ Columns in {project}.{dataset}.{table}:")
    for col in columns:
        print(f"  - {col}")
