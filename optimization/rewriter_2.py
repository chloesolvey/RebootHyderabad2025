import re
from sqlglot import parse_one, exp
from bq_column_resolver import get_table_columns

SUGGESTIONS = []

def check_select_star(expression):
    for select_expr in expression.find_all(exp.Select):
        for proj in select_expr.expressions:
            if isinstance(proj, exp.Star):
                SUGGESTIONS.append("❌ Avoid SELECT * — select only required columns.")
                return True
    return False

def check_extract_year(expression):
    if "EXTRACT(YEAR" in expression.sql().upper():
        SUGGESTIONS.append("❌ EXTRACT disables partition pruning — use DATE_SUB or BETWEEN instead.")
        return True
    return False

def check_nested_subqueries(expression):
    sql = expression.sql().upper()
    if sql.count("SELECT") > 3:
        SUGGESTIONS.append("⚠️ Too many nested subqueries — consider refactoring using CTEs.")
        return True
    return False

def check_join_before_filter(expression):
    joins = list(expression.find_all(exp.Join))
    where = expression.find(exp.Where)
    if joins and where:
        SUGGESTIONS.append("❌ JOIN before WHERE — apply filters before JOINs to reduce data scanned.")
        return True
    return False

def check_rank_usage(expression):
    if "RANK()" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ RANK used — make sure to filter dataset before applying window functions.")
        return True
    return False

def check_percentile_after_join(expression):
    if "PERCENTILE_CONT" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ Use PERCENTILE_CONT on filtered data to avoid full table scan.")
        return True
    return False

def check_cross_joins(expression):
    if "CROSS JOIN" in expression.sql().upper():
        SUGGESTIONS.append("❌ CROSS JOIN used — use INNER JOIN with ON clause instead.")
        return True
    return False

def check_unnest(expression):
    if "UNNEST" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ UNNEST usage — ensure it doesn’t cause cartesian explosion.")
        return True
    return False

def check_regexp_like(expression):
    if "LIKE '%" in expression.sql().upper() or "REGEXP_CONTAINS" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ LIKE/REGEXP_CONTAINS — use indexed fields or avoid prefix wildcards.")
        return True
    return False

def check_udf(expression):
    if "JS(" in expression.sql().upper() or "JS_" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ JavaScript UDF — ensure UDFs are necessary, else prefer native functions.")
        return True
    return False

def check_wildcard_table(expression):
    if re.search(r"FROM\s+`[^`]+`\.\*+", expression.sql(), re.IGNORECASE):
        SUGGESTIONS.append("⚠️ Wildcard table usage detected — ensure it's needed.")
        return True
    return False

def check_approx_functions(expression):
    if "APPROX_" in expression.sql().upper():
        SUGGESTIONS.append("✅ For large datasets, consider APPROX_TOP_COUNT or APPROX_QUANTILES to reduce compute.")
        return True
    return False

def check_group_by_skew(expression):
    if "GROUP BY" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ GROUP BY might cause data skew — consider clustering or partitioning.")
        return True
    return False

def check_max_bytes(expression):
    SUGGESTIONS.append("⚠️ Use max_bytes_billed to prevent unexpected costs in BigQuery.")
    return True

def check_repeat_queries(expression):
    SUGGESTIONS.append("💡 If this query runs often, consider result caching or scheduled materialization.")
    return True

def check_structs(expression):
    if "STRUCT" in expression.sql().upper() or "RECORD" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ Flatten nested fields only when required — use JSON_VALUE or dot notation.")
        return True
    return False

def check_search_function(expression):
    if "SEARCH(" in expression.sql().upper():
        SUGGESTIONS.append("⚠️ SEARCH used — ensure proper indexes exist for efficient lookups.")
        return True
    return False

def check_materialization(expression):
    SUGGESTIONS.append("💡 Consider materializing reused subqueries or expensive joins.")
    return True

# ======================== TEMPLATE GENERATION ========================= #

def generate_optimized_template(sql: str, project_id=None) -> str:
    """
    Generate an optimized SQL template covering all 18 checks
    """
    try:
        expression = parse_one(sql, read='bigquery')
    except Exception:
        return "-- ❌ Unable to parse SQL. Please check syntax."

    SUGGESTIONS.clear()
    checks = [
        check_select_star,
        check_extract_year,
        check_nested_subqueries,
        check_join_before_filter,
        check_rank_usage,
        check_percentile_after_join,
        check_cross_joins,
        check_unnest,
        check_regexp_like,
        check_udf,
        check_wildcard_table,
        check_approx_functions,
        check_group_by_skew,
        check_max_bytes,
        check_repeat_queries,
        check_structs,
        check_search_function,
        check_materialization
    ]

    for check in checks:
        try:
            check(expression)
        except Exception:
            pass

    # Build optimized SQL template (naive heuristic)
    optimized_sql = sql
    if check_select_star(expression):
        # Try to replace SELECT * with resolved columns (if project info provided)
        match = re.search(r'FROM\s+`?(?P<project>[\w\-]+)\.(?P<dataset>\w+)\.(?P<table>\w+)`?', sql, re.IGNORECASE)
        if match and project_id:
            table_info = match.groupdict()
            columns = get_table_columns(project_id, table_info["dataset"], table_info["table"])
            col_str = ",\n    ".join(columns)
            optimized_sql = re.sub(r'SELECT\s+\*', f'SELECT\n    {col_str}', optimized_sql, flags=re.IGNORECASE)

    # Wrap output with suggestions
    suggestions_block = "-- 🧠 Optimized Query with Suggestions\n"
    for msg in SUGGESTIONS:
        suggestions_block += f"-- {msg}\n"
    
    return suggestions_block + "\n" + optimized_sql


# ===================== TEST RUN ====================== #

if __name__ == "__main__":
    test_sql = """
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
      FROM `chrome-inkwell-466604-r4.core_data.transaction_details` t
      LEFT JOIN `chrome-inkwell-466604-r4.core_data.account_details` a 
             ON t.account_id = a.account_id
      LEFT JOIN `chrome-inkwell-466604-r4.core_data.customer_details` c 
             ON a.customer_id = c.customer_id
      WHERE 
        EXTRACT(YEAR FROM CURRENT_DATE()) - EXTRACT(YEAR FROM DATE(a.open_date)) > 3
        AND t.transaction_type IN (
          SELECT DISTINCT transaction_type 
          FROM `chrome-inkwell-466604-r4.core_data.transaction_details`
          WHERE transaction_type IN ('PAYMENT', 'WITHDRAWAL')
        )
    )
  )
  WHERE transaction_amount > (
    SELECT APPROX_QUANTILES(transaction_amount, 100)[OFFSET(90)]
    FROM `chrome-inkwell-466604-r4.core_data.transaction_details`
  )
)
GROUP BY segment, channel
ORDER BY total_amount DESC

    """
    print(generate_optimized_template(test_sql, project_id="chrome-inkwell-466604-r4"))
