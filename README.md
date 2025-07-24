# GreenQuery – Sustainable BigQuery Optimization

This project analyzes BigQuery SQL queries placed in the `/sql/` directory, performs dry runs to estimate cost and data processed, and pushes recommendations to a BigQuery table. It is containerized using Docker and deployed as a Cloud Run Job on Google Cloud Platform (GCP).

---

## 📁 Project Structure

greenquery/
│
├── recommendation/
│ └── [python modules and logic]
│
├── sql/
│ └── [Place your test SQL queries here]
│
├── Dockerfile
├── requirements.txt
└── main.py


---

## 🧾 Usage Instructions

     1️⃣ Add Your SQL Queries

    - Place your SQL queries (only `.sql` files) in the `/sql/` directory.
    - These queries will be picked up by the application for dry run analysis and recommendation generation.
    - Push your changes to the main Git repository.


     2️⃣ Build and Deploy the Cloud Run Job

        From the root of the repository, run the following commands in your terminal (e.g., VSCode terminal):

         🔧 Build and Push Docker Image

        -> gcloud builds submit --tag gcr.io/ltc-reboot25-team-58/greenquery

        🚀 Deploy Cloud Run Job (one-time setup)

         ->  gcloud beta run jobs deploy reboot-repo-job \
            --image gcr.io/ltc-reboot25-team-58/greenquery \
            --region europe-west2 \
            --memory 1Gi \
            --cpu 1 \
            --max-retries 1 \
            --timeout 900s \
            --project ltc-reboot25-team-58

        ▶️ Run the Job
        -> gcloud beta run jobs execute reboot-repo-job \
            --region europe-west2 \
            --project ltc-reboot25-team-58

    3️⃣ ✅ Verify Job Status
        You can monitor execution logs and job status at the following Cloud Console link:

        👉 Cloud Run Job Dashboard: https://console.cloud.google.com/run/jobs/details/europe-west2/reboot-repo-job/executions?authuser=0&inv=1&invt=Ab3mVg&project=ltc-reboot25-team-58

        ✅ Job Configuration
        Job Name: reboot-repo-job
        Region: europe-west2
        Service Account: Must have permissions for BigQuery access
        🐍 Python Version - This project uses Python 3.11

        📦 Dependencies
        Install locally (for development/testing):
        -> pip install -r requirements.txt


📬 Contact

---

Let us know if you'd like a badge section (e.g., Python version, GCP deploy, etc.) or want to include setup for automated GitHub Actions later.
<Engineers Email id>
