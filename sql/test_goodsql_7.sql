SELECT
  a.account_id,
  c.first_name,
  c.last_name,
  a.balance,
  SUM(t.transaction_amount) AS total_spend_90d,
  COUNT(t.txn_id) AS txn_count_90d,
  MAX(t.transaction_date) AS last_transaction_date
FROM
  `core_data.account_details` a
JOIN
  `core_data.customer_details` c USING (customer_id)
JOIN
  `core_data.transaction_details` t
    ON a.account_id = t.account_id
WHERE
  t.transaction_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 90 DAY)
  AND a.status = 'Active'
GROUP BY
  a.account_id,
  c.first_name,
  c.last_name,
  a.balance
ORDER BY
  total_spend_90d DESC
LIMIT 100;
