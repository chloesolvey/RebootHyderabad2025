-- SELECT *
-- FROM `core_data.account_details` a
-- CROSS JOIN `core_data.customer_details` c
-- CROSS JOIN `core_data.transaction_details` t
-- WHERE a.account_id = t.account_id; -- late filtering, joins become a huge Cartesian product first


-- BAD QUERY 3:  Full-outer joins, SELECT *, and formatting the partition column
SELECT *
FROM  `core_data.account_details`      AS a
FULL JOIN `core_data.customer_details` AS c  ON a.customer_id = c.customer_id      -- full Cartesian-style merge
FULL JOIN `core_data.transaction_details` t  ON a.account_id  = t.account_id       -- adds another wide full join
WHERE FORMAT_DATE('%Y-%m-%d', t.transaction_date) LIKE '2025-%'                    -- function on partition column disables pruning
;
