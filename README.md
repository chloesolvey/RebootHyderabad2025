# 🚀 404dnf_repo

> **Automated SQL & BigQuery Analytics Pipeline**
>
> *Analyze, optimize, forecast, and monitor your data workloads in Google BigQuery. Recommendations, costs, sustainability metrics, and operational insights are delivered directly to Looker Studio dashboards.*

---

## ✨ Features

- **SQL Analysis & Recommendations**
  - 📂 Scans all files in the `sql/` directory for new and updated queries.
  - ✅ Performs syntax checks, dry runs, estimates query cost, bytes processed, and CO₂ emitted.
  - 💡 Generates actionable optimization recommendations.

- **Cost, Usage, and Sustainability Forecasting**
  - 📊 Loads results into a BigQuery table for historical insights.
  - 🔮 ML models (see `ml_models/`) forecast usage, cost, and emissions.

- **BigQuery Usage Monitoring**
  - 🛡️ Monitors BigQuery service logs via a log router.
  - 📈 Produces usage metrics, detects anomalies, and extracts trends.

- **Looker Studio Reporting**
  - 📊 All insights, KPIs, and forecasts are analyzed and visualized in beautiful Looker Studio dashboards.

---

## 🗂️ Project Structure

404dnf_repo/
├── alerts/
│ └── alerting_logic.py # BigQuery usage monitors & alert logic
├── ddl/
│ ├── account_details.sql
│ ├── customer_details.sql
│ └── transaction_details.sql # Table schema examples
├── infra/
├── ml_models/ # ML forecasting scripts & models
├── optimization/
├── project_docs/
├── service_accounts/
├── sql/ # MAIN: All SQL files analyzed
├── src/
│ ├── recommendation/
│ │ ├── init.py
│ │ ├── analysis_utils.py # Query checks, dry run logic, etc.
│ │ ├── bigquery_utils.py # GCP & BQ integration helpers
│ │ └── file_utils.py # FS utilities
│ ├── main.py # 🚩 Main application workflow
│ └── tests/
│ ├── tests_analysis_utils.py
│ ├── tests_bigquery_utils.py
│ └── tests_file_utils.py
├── cloudbuild.yaml # CI/CD pipeline config
├── Dockerfile # Containerization
├── essential_commands.txt
├── requirements.txt
└── README.md


---

## 🔧 Getting Started

1. **Install dependencies:**
pip3 install -r requirements.txt


2. **Set up Google Cloud credentials** with permissions for BigQuery, Artifact Registry, Cloud Run, and Logging.

3. **Run the analysis pipeline locally:**
python3 -m src.main


---

## 🚦 Automated Deployment — CI/CD & Cloud Run

- **Cloud Build Trigger:** Watches the `sql/` directory.  
🏗️ On every new commit/change in `sql/`:
 1. The Docker container is built (`Dockerfile`).
 2. The image is pushed to **Google Artifact Registry**.
 3. A **Cloud Run Job** is created/updated **and** executed, running the entire pipeline using your latest code and queries.
 4. All new results are loaded into BigQuery for reporting and forecasting.

**cloudbuild.yaml** (snippet):
steps:

name: 'gcr.io/cloud-builders/docker'
args: ['build', '-t', 'REGION-docker.pkg.dev/PROJECT_ID/REPO_NAME/image-name:$COMMIT_SHA', '.']

name: 'gcr.io/cloud-builders/docker'
args: ['push', 'REGION-docker.pkg.dev/PROJECT_ID/REPO_NAME/image-name:$COMMIT_SHA']

name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
entrypoint: 'gcloud'
args:
[
'run', 'jobs', 'deploy', 'bq-analysis-job',
'--image', 'REGION-docker.pkg.dev/PROJECT_ID/REPO_NAME/image-name:$COMMIT_SHA',
'--region', 'YOUR_REGION',
'--project', '$PROJECT_ID'
]

name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
entrypoint: 'gcloud'
args:
[
'run', 'jobs', 'execute', 'bq-analysis-job',
'--region', 'YOUR_REGION',
'--project', '$PROJECT_ID'
]
options:
logging: CLOUD_LOGGING_ONLY

- Replace `REGION`, `PROJECT_ID`, `REPO_NAME`, and `YOUR_REGION` appropriately.

---

## 📊 Data & Reporting Flow

- 📥 **Analysis & Recommendations:** All SQL checked, costs & sustainability estimated.
- 🏦 **BigQuery Data Lake:** All results stored for insights and further ML modeling.
- 📈 **Looker Studio:** Visualize insights, cost trends, forecasting, and environmental impact.
- 🛡️ **Operational Monitoring:** All BigQuery activity and anomalies are tracked and alertable.

---

## 🤝 Contributing

1. Fork the repository and branch off your feature.
2. Add or update tests in `src/tests/`.
3. Open a pull request with a concise description of your additions.

---

## 🛡️ License

LTC_reboot_2025_404dnf_engg

---

**Professional-grade analytics, FinOps, and cost/sustainability governance for your cloud data stack — powered by Python & Google Cloud.**

*For setup help, see `essential_commands.txt` or documentation in `project_docs/`. For support or ideas, open a GitHub issue!*
