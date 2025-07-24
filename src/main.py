import os
from datetime import datetime
import pandas as pd
import logging

from recommendation.file_utils import read_sql_file, classify_sql_statements
from recommendation.analysis_utils import analyze_sql, estimate_carbon_emission, generate_record_hash
from recommendation.bigquery_utils import run_dry_run, create_bq_table_if_not_exists, load_dataframe_to_bq_upsert

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def process_sql_directory(sql_dir="sql"):
    """
    Recursively process all .sql files in directory to identify SELECT queries,
    run dry run for cost & bytes processed, analyze SQL for recommendations,
    and produce records ready for BigQuery load.
    """
    records = []
    now = datetime.utcnow()

    for root, _, files in os.walk(sql_dir):
        for file in files:
            if file.endswith('.sql'):
                path = os.path.join(root, file)
                logger.info(f"Processing {path}")
                sql_text = read_sql_file(path)
                classified = classify_sql_statements(sql_text)

                for select_sql in classified['select']:
                    bytes_processed, cost_usd = run_dry_run(select_sql)
                    if bytes_processed is None:
                        recs = ["Unable to run dry run for this query, possibly syntax or permission issues."]
                        processed_gb = cost_usd = carbon_kg = None
                    else:
                        recs = analyze_sql(select_sql)
                        processed_gb = round(bytes_processed / (1024**3), 3)
                        cost_usd = round(cost_usd, 4)
                        carbon_kg = round(estimate_carbon_emission(bytes_processed), 6)

                    # Only record queries which have recommendations
                    if recs:
                        rec_hash = generate_record_hash(path, recs)
                        records.append({
                            "record_hash": rec_hash,
                            "file": path,
                            "processed_gb": processed_gb,
                            "estimated_cost_usd": cost_usd,
                            "estimated_carbon_kg": carbon_kg,
                            "recommendations": recs,
                            "load_time_utc": now,
                        })
    return records

def main():
    dataset = "greenquery_core"
    table = "dryrun_analysis"

    records = process_sql_directory()
    if not records:
        logger.info("No recommendations generated.")
        return
    
    logger.info(f"{len(records)} queries analyzed with recommendations.")
    df = pd.DataFrame(records)

    create_bq_table_if_not_exists(dataset, table)
    load_dataframe_to_bq_upsert(df, dataset, table)
    logger.info(f"Successfully loaded {len(df)} records to {dataset}.{table}")

if __name__ == "__main__":
    main()
