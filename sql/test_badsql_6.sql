-- BAD QUERY 2: Per-row scalar subquery + function calls on clustered keys
SELECT
  a.account_id,
  c.first_name,
  (SELECT SUM(t2.transaction_amount)                      -- runs for every account row
   FROM `core_data.transaction_details` t2
   WHERE CAST(t2.account_id AS STRING) = CAST(a.account_id AS STRING)
  ) AS total_spend,
  a.balance
FROM `core_data.account_details`  a
JOIN `core_data.customer_details` c
  ON CAST(c.customer_id AS STRING) = CAST(a.customer_id AS STRING);  -- disables clustering
