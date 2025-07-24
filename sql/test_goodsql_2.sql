SELECT
  a.branch_code,
  COUNT(DISTINCT a.account_id) AS num_accounts,
  SUM(a.balance) AS total_balance,
  AVG(a.balance) AS avg_balance
FROM
  `core_data.account_details` a
WHERE
  a.status = 'active'
GROUP BY
  a.branch_code
ORDER BY
  total_balance DESC
LIMIT 10;
