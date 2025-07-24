SELECT
  c.customer_id,
  c.first_name,
  a.account_id,
  a.balance,
  a.account_type,
  a.last_withdrawal_date
FROM
  `core_data.customer_details` c
JOIN
  `core_data.account_details` a
  ON c.customer_id = a.customer_id
WHERE
  a.balance > 0
  AND a.last_withdrawal_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR)
LIMIT 500;
