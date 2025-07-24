# Carbon Analyser & Reporter (CAR) System

## Overview

The Carbon Analyser & Reporter (CAR) system is designed to process, analyze, and visualize carbon-related data. [cite_start]It provides end-users with a React-based frontend to access insights, leveraging Google Cloud Platform (GCP) for backend infrastructure, data storage, and processing, and Looker for embedded analytics[cite: 26, 42]. [cite_start]Data is ingested from external sources, orchestrated via Apache Airflow (Cloud Composer), and stored in BigQuery for analytical querying.

## Architecture

[cite_start]The CAR system's architecture comprises several key components working in a circular flow[cite: 2, 4]:

* [cite_start]**Users (1):** End users interact with the platform through a React-based frontend[cite: 18, 19, 28].
* [cite_start]**Authentication (2):** Secure access is enabled using JWT (JSON Web Token) tokens for secure, token-based access to the Looker platform[cite: 24, 25, 31, 32].
* **Frontend (React) (3):** The user interface is a React application that integrates with Looker via the Looker Embed SDK to display embedded dashboards and analytics. [cite_start]It also interacts with DevOps tools like GitHub, Docker, and Kubernetes[cite: 22, 23, 35, 36, 37, 38, 39].
* **Looker (Embedded Analytics) (4):** Looker serves as the data visualization and analytics layer, embedded within the React app to provide dashboards and charts. [cite_start]It fetches data directly from Google BigQuery and does not store data itself[cite: 20, 21, 42, 43, 44].
* **GCP Backend Infrastructure (5):** All cloud resources are hosted on Google Cloud Platform. This includes:
    * [cite_start]**Cloud Storage:** Stores raw or pre-processed data files received via API[cite: 9, 10, 11, 48].
    * [cite_start]**BigQuery:** Functions as the main data warehouse for structured and analytical queries[cite: 49].
    * [cite_start]**Cloud Composer (Apache Airflow):** Orchestrates workflows, managing ETL (Extract, Transform, Load) jobs and scheduling data pipelines[cite: 51].
* [cite_start]**DESNZ Server (6):** An external government or agency server (DESNZ) sends API requests to upload or push datasets to the CAR system[cite: 1, 5, 52].

## Data Flow (7)

[cite_start]The data flow within the CAR system follows a clear path from source to visualization[cite: 4, 54, 55]:

1.  [cite_start]Raw data is sent from the DESNZ server to Cloud Storage[cite: 56].
2.  [cite_start]This data is then orchestrated by Cloud Composer (Apache Airflow) and loaded into BigQuery[cite: 53, 56].
3.  [cite_start]Looker queries BigQuery to generate visual insights[cite: 57].
4.  [cite_start]The React frontend displays these insights to end users through the Looker Embed SDK[cite: 57].

## Key Technologies Used

* [cite_start]**Frontend:** React, JWT Auth, Looker Embed SDK [cite: 59]
* [cite_start]**Backend:** Google Cloud (Cloud Storage, BigQuery, Cloud Composer) [cite: 60]
* [cite_start]**Data Visualization:** Looker [cite: 60]
* [cite_start]**External Integration:** DESNZ API [cite: 61]
