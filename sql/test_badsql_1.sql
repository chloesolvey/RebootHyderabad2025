SELECT
  *
FROM
  `core_data.account_details` a
CROSS JOIN
  `core_data.customer_details` c
CROSS JOIN
  `core_data.transaction_details` t
WHERE
  a.account_id = t.account_id

