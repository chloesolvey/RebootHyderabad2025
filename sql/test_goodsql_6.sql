-- GOOD QUERY 2: Pre-aggregate once, then join; preserves clustering keys
WITH txn_agg AS (
  SELECT
    account_id,
    SUM(transaction_amount) AS total_spend,
    MAX(transaction_date)   AS last_txn_date
  FROM `core_data.transaction_details`
  WHERE transaction_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 365 DAY)
  GROUP BY account_id
)
SELECT
  a.account_id,
  a.balance,
  c.first_name,
  c.last_name,
  x.total_spend,
  x.last_txn_date
FROM `core_data.account_details`  a
JOIN txn_agg                     x USING (account_id)
JOIN `core_data.customer_details` c ON a.customer_id = c.customer_id
WHERE a.status = 'Active';
