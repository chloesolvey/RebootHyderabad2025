-- GOOD QUERY 1: Proper joins, partition pruning, limited columns, aggregation
SELECT
  a.account_id,
  c.first_name,
  c.last_name,
  SUM(t.transaction_amount) AS total_spend_90d,
  COUNT(*)                  AS txn_cnt_90d
FROM  `core_data.account_details`     a
JOIN  `core_data.customer_details`    c ON a.customer_id = c.customer_id
JOIN  `core_data.transaction_details` t ON a.account_id  = t.account_id
WHERE t.transaction_date BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 90 DAY) AND CURRENT_DATE()
  AND a.status = 'Active'
GROUP BY a.account_id, c.first_name, c.last_name
ORDER BY total_spend_90d DESC
LIMIT 100;
