CREATE TABLE `core_data.account_details`
(
  account_id STRING NOT NULL,
  customer_id STRING NOT NULL,
  account_number STRING,
  account_type STRING,
  open_date DATE,
  branch_code STRING,
  branch_name STRING,
  balance FLOAT64,
  available_balance FLOAT64,
  currency STRING,
  status STRING,
  interest_rate FLOAT64,
  overdraft_limit FLOAT64,
  is_salary_account BOOL,
  joint_account BOOL,
  nominee_name STRING,
  nominee_relation STRING,
  kyc_status STRING,
  kyc_update_date DATE,
  last_deposit_date DATE,
  last_withdrawal_date DATE,
  total_deposits FLOAT64,
  total_withdrawals FLOAT64,
  account_manager STRING,
  closed_date DATE,
  channel_opened STRING,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)
PARTITION BY open_date
CLUSTER BY customer_id, account_type, branch_code;
