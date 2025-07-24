SELECT
  c.customer_id,
  c.first_name,
  FORMAT_DATE('%Y-%m', t.transaction_date) AS month,
  COUNT(t.txn_id) AS transaction_count,
  SUM(t.fee) AS total_amount,
  AVG(t.transaction_amount) AS avg_transaction_amount
FROM
  `core_data.customer_details` c
JOIN
  `core_data.account_details` a
  ON c.customer_id = a.customer_id
JOIN
  `core_data.transaction_details` t
  ON a.account_id = t.account_id
WHERE
  c.status = 'active'
  AND t.transaction_date BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH) AND CURRENT_DATE()
GROUP BY
  c.customer_id,
  c.first_name,
  month
ORDER BY
  month DESC,
  total_amount DESC
LIMIT 1000;
