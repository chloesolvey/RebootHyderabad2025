import unittest
from src.recommendation.analysis_utils import analyze_sql, estimate_carbon_emission, generate_record_hash

class TestAnalysisUtils(unittest.TestCase):
    def test_invalid_sql(self):
        # Should flag invalid/malformed SQL.
        self.assertIn("Invalid SQL syntax", analyze_sql("SELECT FROM WHERE")[0])

    def test_select_star(self):
        sql = "SELECT * FROM foo WHERE _PARTITIONDATE='2024-01-01'"
        recs = analyze_sql(sql)
        self.assertTrue(any("Avoid SELECT *" in r for r in recs))

    def test_missing_where(self):
        sql = "SELECT col FROM t"
        recs = analyze_sql(sql)
        self.assertTrue(any("No WHERE clause found" in r for r in recs))

    def test_join_on_missing(self):
        sql = "SELECT * FROM a JOIN b"
        recs = analyze_sql(sql)
        self.assertTrue(any("lack ON clause" in r for r in recs))

    def test_partition_filter_present(self):
        sql = "SELECT col FROM t WHERE _PARTITIONTIME = '2024-07-23'"
        recs = analyze_sql(sql)
        self.assertFalse(any("Add filters on partition columns" in r for r in recs))

    def test_aggregation_without_group(self):
        sql = "SELECT COUNT(*) FROM t"
        recs = analyze_sql(sql)
        self.assertTrue(any("Aggregations used without GROUP BY" in r for r in recs))

    def test_aggregation_with_group(self):
        sql = "SELECT COUNT(*), c FROM t GROUP BY c"
        recs = analyze_sql(sql)
        self.assertFalse(any("Aggregations used without GROUP BY" in r for r in recs))

    def test_order_by_detected(self):
        sql = "SELECT c FROM t ORDER BY c"
        recs = analyze_sql(sql)
        self.assertTrue(any("ORDER BY found" in r for r in recs))

    def test_distinct_detection(self):
        sql = "SELECT DISTINCT c FROM t"
        recs = analyze_sql(sql)
        self.assertTrue(any("DISTINCT detected" in r for r in recs))

    def test_cte_detection(self):
        sql = "WITH foo as (SELECT 1) SELECT * FROM foo"
        recs = analyze_sql(sql)
        self.assertTrue(any("CTE (WITH statement) used" in r for r in recs))

    def test_window_function_detection(self):
        sql = "SELECT ROW_NUMBER() OVER (ORDER BY c) FROM t"
        recs = analyze_sql(sql)
        self.assertTrue(any("Window functions detected" in r for r in recs))

    def test_union_vs_union_all(self):
        sql_union = "SELECT c FROM t1 UNION SELECT c FROM t2"
        sql_union_all = "SELECT c FROM t1 UNION ALL SELECT c FROM t2"
        self.assertTrue(any("Prefer UNION ALL" in r for r in analyze_sql(sql_union)))
        self.assertFalse(any("Prefer UNION ALL" in r for r in analyze_sql(sql_union_all)))

    def test_cast_and_computed_column_detection(self):
        sql = "SELECT CAST(col AS STRING), SUBSTR(col,1,2) FROM t"
        recs = analyze_sql(sql)
        self.assertTrue(any("Casting to STRING" in r for r in recs))
        self.assertTrue(any("Computed columns detected" in r for r in recs))

    def test_view_usage_detection(self):
        sql = "SELECT * FROM `proj.ds.table_view`"
        recs = analyze_sql(sql)
        self.assertTrue(any("Query accesses views" in r for r in recs))

    def test_early_filtering_on_join_key(self):
        sql = "SELECT * FROM a JOIN b ON a.id = b.id WHERE a.id=1"
        recs = analyze_sql(sql)
        self.assertFalse(any("No filtering on join keys" in r for r in recs))

    def test_early_filtering_missing(self):
        sql = "SELECT * FROM a JOIN b ON a.id = b.id WHERE b.name='X'"
        recs = analyze_sql(sql)
        self.assertTrue(any("No filtering on join keys detected before JOINs" in r for r in recs))

    def test_complexity_levels(self):
        sql_low = "SELECT col FROM t WHERE col=1"
        sql_med = "SELECT DISTINCT a.col, b.col FROM a JOIN b ON a.k=b.k"
        sql_high = """WITH cte1 AS (SELECT * FROM t1), 
                      cte2 AS (SELECT * FROM t2)
                      SELECT DISTINCT col, ROW_NUMBER() OVER (ORDER BY x)
                      FROM cte1 JOIN cte2 ON cte1.id = cte2.id"""
        self.assertTrue(any("Low" in r for r in analyze_sql(sql_low)))
        self.assertTrue(any("Medium" in r for r in analyze_sql(sql_med)))
        self.assertTrue(any("High" in r for r in analyze_sql(sql_high)))

    def test_context_cost_freq(self):
        sql = "SELECT col FROM t WHERE _PARTITIONTIME=CURRENT_DATE()"
        recs1 = analyze_sql(sql, query_stats={'frequency': 5})
        recs2 = analyze_sql(sql, query_stats={'frequency': 150, 'average_cost': 15*(1024**3)})
        self.assertFalse(any("runs frequently" in r for r in recs1))
        self.assertTrue(any("runs frequently" in r for r in recs2))
        self.assertTrue(any("large data volume" in r for r in recs2))

    def test_estimate_carbon_emission(self):
        # 1GB --> 0.0005kg, zero for zero bytes
        self.assertAlmostEqual(estimate_carbon_emission(1024 ** 3), 0.0005)
        self.assertEqual(estimate_carbon_emission(0), 0.0)

    def test_generate_record_hash(self):
        # Should be SHA256, stable for reordered recs
        recs1 = ["a", "b", "c"]
        recs2 = ["c", "b", "a"]
        h1 = generate_record_hash("file.sql", recs1)
        h2 = generate_record_hash("file.sql", recs2)
        self.assertEqual(h1, h2)
        self.assertEqual(len(h1), 64)
