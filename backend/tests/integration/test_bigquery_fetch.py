import unittest
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../src")))
from src.utils.bigquery_reader import run_query

class TestBigQueryFetch(unittest.TestCase):

    def test_fetch_conversion_rates(self):
        query = """
            SELECT category, sub_category, rate_per_unit
            FROM `your_project.dataset.conversion_rates`
            WHERE category = 'electricity_usage'
        """
        results = run_query(query)
        self.assertIsInstance(results, list)
        self.assertGreater(len(results), 0)
        self.assertIn('rate_per_unit', results[0])

    def test_emission_factors(self):
        query = """
            SELECT activity_type, sub_activity, kg_co2e_per_unit
            FROM `your_project.dataset.emission_factors`
            WHERE activity_type = 'fuel_consumption'
        """
        results = run_query(query)
        self.assertTrue(any(r['kg_co2e_per_unit'] > 0 for r in results))

if __name__ == "__main__":
    unittest.main()
