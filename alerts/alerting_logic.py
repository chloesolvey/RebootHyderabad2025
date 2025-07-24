import os
from datetime import datetime
import pandas as pd
import requests

from recommendation.file_utils import read_sql_file, classify_sql_statements
from recommendation.analysis_utils import analyze_sql, estimate_carbon_emission, generate_record_hash
from recommendation.bigquery_utils import run_dry_run, create_bq_table_if_not_exists, load_dataframe_to_bq_upsert

# ---- CONFIG: set your webhook ----
SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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"
COVERAGE_THRESHOLD = 80

# Patterns that trigger alert
CRITICAL_PATTERNS = [
    "Avoid SELECT *",
    "No WHERE clause found",
    "JOINs lack ON clause",
    "Estimated query complexity level: High",
    "Use of expensive function",
    "Aggregation used without GROUP BY",
    "Add filters on partition columns",
    "Query processes large data volume",
    "Query runs frequently",
    "Query accesses views",
]

def should_alert_on_recommendations(recommendations):
    """Return True, alert_lines if any critical pattern matches."""
    alert_lines = [rec for rec in recommendations if any(p in rec for p in CRITICAL_PATTERNS)]
    return bool(alert_lines), alert_lines

def send_slack_alert(header, alert_lines, all_recommendations, file_path=None):
    blocks = [{
        "type": "section",
        "text": {"type": "mrkdwn", "text": f":rotating_light: *{header}*"}
    }]
    if file_path:
        blocks.append({
            "type": "section",
            "text": {"type": "mrkdwn", "text": f"File: `{file_path}`"}
        })
    blocks.append({
        "type": "section",
        "text": {"type": "mrkdwn", "text": "*Critical findings:*\n" + "\n".join(f"• {line}" for line in alert_lines)}
    })
    blocks.append({
        "type": "section",
        "text": {"type": "mrkdwn", "text": "*All recommendations:*\n" + "\n".join(f"- {line}" for line in all_recommendations)}
    })

    requests.post(SLACK_WEBHOOK_URL, json={"blocks": blocks})

def process_sql_directory_and_alert(sql_dir="sql"):
    """
    Process all .sql files, analyze and alert if risky patterns are found.
    """
    records = []
    now = datetime.utcnow()
    for root, _, files in os.walk(sql_dir):
        for file in files:
            if file.endswith('.sql'):
                path = os.path.join(root, file)
                print(f"Processing {path}")
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

                    # Alert if any critical issues detected
                    alert, alert_lines = should_alert_on_recommendations(recs)
                    if alert:
                        send_slack_alert(
                            header="Potential Risky Query Detected",
                            alert_lines=alert_lines,
                            all_recommendations=recs,
                            file_path=path
                        )
                        print(f"Slack alert sent for {path}")

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

def send_coverage_slack_alert(coverage_percent, per_file_dict):
    # Optionally send a coverage warning if under threshold
    if coverage_percent < COVERAGE_THRESHOLD:
        files_str = "\n".join(f"- `{fname}`: {pct}%" for fname, pct in per_file_dict.items())
        text = (
            f":x: *Coverage Alert*\n"
            f"Overall coverage: *{coverage_percent}%* (threshold: {COVERAGE_THRESHOLD}%)\n"
            f"Per file:\n{files_str}\n"
            f"Please increase test coverage."
        )
        requests.post(SLACK_WEBHOOK_URL, json={"text": text})

def main():
    dataset = "greenquery_core"
    table = "dryrun_analysis"
    records = process_sql_directory_and_alert()
    if not records:
        print("No recommendations generated.")
        return
    df = pd.DataFrame(records)
    create_bq_table_if_not_exists(dataset, table)
    load_dataframe_to_bq_upsert(df, dataset, table)

    # Optionally report coverage after pipeline
    try:
        import subprocess
        import re
        output = subprocess.run(['coverage', 'report'], capture_output=True, text=True).stdout
        match = re.search(r'Total\s+\d+\s+\d+\s+\d+\s+(\d+)%', output)
        if match:
            percent = int(match.group(1))
            per_file = {}
            for line in output.split('\n'):
                m = re.match(r'(\S+)\s+\d+\s+\d+\s+\d+\s+(\d+)%', line)
                if m:
                    per_file[m.group(1)] = int(m.group(2))
            send_coverage_slack_alert(percent, per_file)
    except Exception:
        print("Coverage alert not sent (could not gather report).")

if __name__ == "__main__":
    main()
