SELECT
  account_id, t.transaction_id, c.customer_name
FROM
  `core_data.account_details` a,
  UNNEST(a.transaction_ids) t,    -- assuming transaction_ids is an array
  UNNEST(a.customer_ids) c
