import re
import sqlparse
from sqlparse.sql import IdentifierList, Identifier, Where, Comparison
from sqlparse.tokens import Keyword, DML, Token

class BigQueryQueryRewriter:
    def __init__(self, sql: str):
        self.original_sql = sql
        self.suggestions = []
        self.optimized_sql = sql  # For now, we don’t rewrite fully, just annotate

    def detect_select_star(self):
        if re.search(r"SELECT\s+\*", self.original_sql, re.IGNORECASE):
            self.suggestions.append("❌ Avoid SELECT * — select only required columns to reduce scanned bytes.")

    def detect_nested_subqueries(self):
        if self.original_sql.upper().count("SELECT") > 3:
            self.suggestions.append("⚠️ Too many nested subqueries — consider refactoring using CTEs.")

    def detect_partition_pruning(self):
        if re.search(r"EXTRACT\(YEAR.*FROM.*\)", self.original_sql, re.IGNORECASE):
            self.suggestions.append("❌ EXTRACT disables partition pruning — use DATE_SUB or BETWEEN instead.")

    def detect_filter_before_join(self):
        if "JOIN" in self.original_sql.upper() and "WHERE" in self.original_sql.upper():
            join_pos = self.original_sql.upper().index("JOIN")
            where_pos = self.original_sql.upper().index("WHERE")
            if join_pos < where_pos:
                self.suggestions.append("❌ JOIN before WHERE — apply filters before JOINs to reduce data scanned.")

    def detect_rank_usage(self):
        if re.search(r"RANK\(\)", self.original_sql, re.IGNORECASE):
            self.suggestions.append("⚠️ RANK used — make sure to filter dataset before applying window functions.")

    def detect_percentile_pushdown(self):
        if re.search(r"PERCENTILE_CONT", self.original_sql, re.IGNORECASE):
            self.suggestions.append("⚠️ Use PERCENTILE_CONT on filtered data to avoid full table scan.")

    def detect_materialization_need(self):
        if self.original_sql.upper().count("JOIN") > 2:
            self.suggestions.append("💡 Consider materializing expensive joins or reused subqueries into temp tables.")

    def detect_cross_joins(self):
        if re.search(r"CROSS\s+JOIN", self.original_sql, re.IGNORECASE):
            self.suggestions.append("❌ CROSS JOIN detected — this can multiply data. Ensure it is intended.")

    def detect_unnest_usage(self):
        if re.search(r"UNNEST\(", self.original_sql, re.IGNORECASE):
            self.suggestions.append("✅ UNNEST used — ensure it’s necessary and apply filtering before UNNEST where possible.")

    def detect_regexp_like_usage(self):
        if re.search(r"REGEXP_CONTAINS|LIKE\s+['\"]%", self.original_sql, re.IGNORECASE):
            self.suggestions.append("💡 Consider using SEARCH() with indexes instead of REGEXP/LIKE for better performance.")

    def detect_js_udf_usage(self):
        if re.search(r"CREATE\s+TEMPORARY\s+FUNCTION.*LANGUAGE\s+js", self.original_sql, re.IGNORECASE):
            self.suggestions.append("⚠️ JavaScript UDFs are used — may cause performance degradation.")

    def detect_wildcard_tables(self):
        if re.search(r"FROM\s+`[^`]+\.\*`", self.original_sql):
            self.suggestions.append("⚠️ Wildcard tables used — this can be expensive if scanning too many tables.")

    def suggest_approx_functions(self):
        if re.search(r"COUNT\(\*\)|PERCENTILE_CONT", self.original_sql):
            self.suggestions.append("✅ For large datasets, consider APPROX_TOP_COUNT or APPROX_QUANTILES to reduce compute.")

    def detect_data_skew(self):
        if re.search(r"GROUP\s+BY", self.original_sql, re.IGNORECASE):
            self.suggestions.append("💡 GROUP BY detected — monitor for data skew and imbalance. Consider approximate or stratified sampling if needed.")

    def detect_cost_controls(self):
        if "max_bytes_billed" not in self.original_sql:
            self.suggestions.append("⚠️ Use max_bytes_billed to prevent unexpected costs in BigQuery.")

    def detect_caching_opportunity(self):
        if re.search(r"--\s*repeatable_query", self.original_sql, re.IGNORECASE):
            self.suggestions.append("💡 Consider caching or storing repeated query results to save cost.")

    def detect_nested_structs(self):
        if re.search(r"SELECT\s+\w+\.\*", self.original_sql):
            self.suggestions.append("⚠️ Nested STRUCT detected — consider flattening or extracting only necessary fields.")

    def detect_search_index_opportunity(self):
        if "SEARCH(" in self.original_sql.upper():
            self.suggestions.append("✅ SEARCH() used — ensure a search index exists for target fields.")
        elif re.search(r"WHERE\s+.*LIKE\s+['\"]%", self.original_sql, re.IGNORECASE):
            self.suggestions.append("💡 Consider using SEARCH() with index instead of LIKE or REGEXP.")

    def run_all_checks(self):
        self.detect_select_star()
        self.detect_nested_subqueries()
        self.detect_partition_pruning()
        self.detect_filter_before_join()
        self.detect_rank_usage()
        self.detect_percentile_pushdown()
        self.detect_materialization_need()
        self.detect_cross_joins()
        self.detect_unnest_usage()
        self.detect_regexp_like_usage()
        self.detect_js_udf_usage()
        self.detect_wildcard_tables()
        self.suggest_approx_functions()
        self.detect_data_skew()
        self.detect_cost_controls()
        self.detect_caching_opportunity()
        self.detect_nested_structs()
        self.detect_search_index_opportunity()

    def annotate_query(self):
        annotated_query = "-- 🧠 Optimized Query with Suggestions\n"
        for suggestion in self.suggestions:
            annotated_query += f"-- {suggestion}\n"
        annotated_query += "\n" + self.original_sql
        return annotated_query


# Sample usage
if __name__ == "__main__":
    sample_query = """
    SELECT *
    FROM (
      SELECT *, RANK() OVER (PARTITION BY customer_id ORDER BY transaction_amount DESC) as txn_rank
      FROM (
        SELECT *
        FROM transaction_details t
        JOIN account_details a ON t.account_id = a.account_id
        JOIN customer_details c ON a.customer_id = c.customer_id
      )
      WHERE EXTRACT(YEAR FROM DATE(a.open_date)) < 2020
        AND t.transaction_type IN ('PAYMENT', 'WITHDRAWAL')
    )
    WHERE transaction_amount > (
      SELECT PERCENTILE_CONT(transaction_amount, 0.9) FROM transaction_details
    )
    """

    rewriter = BigQueryQueryRewriter(sample_query)
    rewriter.run_all_checks()
    print(rewriter.annotate_query())
