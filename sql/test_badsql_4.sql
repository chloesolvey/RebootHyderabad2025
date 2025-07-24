SELECT
  segment,
  channel,
  COUNT(*) AS txn_count,
  SUM(transaction_amount) AS total_amount,
  AVG(transaction_amount) AS avg_amount
FROM (
  SELECT * 
  FROM (
    SELECT *,
      RANK() OVER (PARTITION BY customer_id ORDER BY transaction_amount DESC) AS txn_rank
    FROM (
      SELECT 
        t.*, 
        a.account_id, 
        a.open_date, 
        c.first_name, 
        c.last_name, 
        CONCAT(c.first_name, ' ', c.last_name) AS customer_name, 
        c.dob,
        c.annual_income,
        c.credit_score,
        c.address,
        c.status,
        CASE
          WHEN c.annual_income > 1000000 THEN 'Premium'
          WHEN c.annual_income BETWEEN 750000 AND 1000000 THEN 'Gold'
          ELSE 'Silver'
        END AS segment
      FROM `core_data.transaction_details` t
      LEFT JOIN `core_data.account_details` a 
             ON t.account_id = a.account_id
      LEFT JOIN `core_data.customer_details` c 
             ON a.customer_id = c.customer_id
      WHERE 
        EXTRACT(YEAR FROM CURRENT_DATE()) - EXTRACT(YEAR FROM DATE(a.open_date)) > 3
        AND t.transaction_type IN (
          SELECT DISTINCT transaction_type 
          FROM `core_data.transaction_details`
          WHERE transaction_type IN ('PAYMENT', 'WITHDRAWAL')
        )
    )
  )
  WHERE transaction_amount > (
    SELECT APPROX_QUANTILES(transaction_amount, 100)[OFFSET(90)]
    FROM `core_data.transaction_details`
  )
)
GROUP BY segment, channel
ORDER BY total_amount DESC
