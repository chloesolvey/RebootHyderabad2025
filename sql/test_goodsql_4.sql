WITH filtered_transactions AS (
  SELECT
    t.txn_id,
    t.transaction_date,
    t.transaction_type,
    t.transaction_amount,
    t.channel,
    t.account_id
  FROM `core_data.transaction_details` t
  WHERE t.transaction_type IN ('PAYMENT', 'WITHDRAWAL')
    AND t.transaction_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 3 YEAR)
),

high_income_customers AS (
  SELECT
    c.customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.annual_income AS income,
    CASE
      WHEN c.annual_income > 1000000 THEN 'Premium'
      WHEN c.annual_income BETWEEN 750000 AND 1000000 THEN 'Gold'
      ELSE 'Silver'
    END AS segment
  FROM `core_data.customer_details` c
  WHERE c.annual_income > 500000
),

eligible_accounts AS (
  SELECT
    a.account_id,
    a.customer_id,
    a.account_type,
    a.open_date
  FROM `core_data.account_details` a
  WHERE a.open_date < DATE_SUB(CURRENT_DATE(), INTERVAL 3 YEAR)
),

joined_data AS (
  SELECT
    f.txn_id,
    f.transaction_date,
    f.transaction_type,
    f.transaction_amount,
    f.channel,
    a.account_type,
    c.customer_id,
    c.customer_name,
    c.segment
  FROM filtered_transactions f
  JOIN eligible_accounts a ON f.account_id = a.account_id
  JOIN high_income_customers c ON a.customer_id = c.customer_id
),

percentiles AS (
  SELECT
    APPROX_QUANTILES(transaction_amount, 100)[OFFSET(90)] AS top10_threshold
  FROM joined_data
),

final_result AS (
  SELECT
    j.*,
    RANK() OVER (PARTITION BY customer_id ORDER BY transaction_amount DESC) as txn_rank
  FROM joined_data j
  JOIN percentiles p ON j.transaction_amount > p.top10_threshold
)

SELECT
  segment,
  channel,
  COUNT(*) AS txn_count,
  SUM(transaction_amount) AS total_amount,
  AVG(transaction_amount) AS avg_amount
FROM final_result
GROUP BY segment, channel
ORDER BY total_amount DESC
