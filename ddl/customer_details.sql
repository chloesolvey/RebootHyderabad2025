CREATE TABLE `core_data.customer_details`
(
  customer_id STRING NOT NULL,
  first_name STRING,
  last_name STRING,
  gender STRING,
  dob DATE,
  email STRING,
  phone STRING,
  address STRING,
  city STRING,
  state STRING,
  postal_code STRING,
  country STRING,
  national_id STRING,
  marital_status STRING,
  occupation STRING,
  company STRING,
  annual_income FLOAT64,
  credit_score INT64,
  preferred_language STRING,
  join_date DATE,
  status STRING,
  last_active DATETIME,
  marketing_opt_in BOOL,
  referred_by STRING,
  consent_email_marketing BOOL,
  risk_profile STRING,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)
PARTITION BY DATE(_PARTITIONTIME)
CLUSTER BY city, country, last_name;
