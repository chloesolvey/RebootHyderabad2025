SELECT
  account_id,
  (SELECT COUNT(*) FROM `core_data.transaction_details` t WHERE t.account_id = a.account_id) as txn_count,*
FROM
  `core_data.account_details` a