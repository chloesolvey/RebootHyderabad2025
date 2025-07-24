from typing import List, Dict
from google.cloud import bigquery

def run_query(query: str) -> List[Dict]:
    client = bigquery.Client()
    query_job = client.query(query)
    return [dict(row) for row in query_job.result()]

