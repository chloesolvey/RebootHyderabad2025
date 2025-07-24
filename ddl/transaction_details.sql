CREATE TABLE `core_data.transaction_details`
(
  txn_id STRING NOT NULL,
  account_id STRING NOT NULL,
  customer_id STRING NOT NULL,
  transaction_date DATE,
  transaction_time TIME,
  transaction_type STRING,
  transaction_amount FLOAT64,
  balance_after_txn FLOAT64,
  description STRING,
  channel STRING,
  status STRING,
  initiated_by STRING,
  approved_by STRING,
  ref_number STRING,
  is_reversal BOOL,
  location STRING,
  device_id STRING,
  currency STRING,
  merchant STRING,
  remarks STRING,
  fee FLOAT64,
  tax FLOAT64,
  txn_medium STRING,
  atm_id STRING,
  reversal_reason STRING,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)
PARTITION BY transaction_date
CLUSTER BY account_id, transaction_type;
