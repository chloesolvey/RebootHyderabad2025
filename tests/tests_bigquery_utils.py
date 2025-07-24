import unittest
from unittest.mock import patch, MagicMock
import pandas as pd


class TestBigQueryUtils(unittest.TestCase):

    @patch('src.recommendation.bigquery_utils.client')  # Patch the module-level BigQuery client instance
    def test_run_dry_run_success(self, mock_client):
        mock_query_job = MagicMock()
        mock_query_job.total_bytes_processed = 1073741824  # 1GB
        mock_client.query.return_value = mock_query_job

        from src.recommendation.bigquery_utils import run_dry_run
        bytes_processed, cost = run_dry_run("SELECT 1")

        self.assertEqual(bytes_processed, 1073741824)
        self.assertAlmostEqual(cost, (1073741824 / (1024 ** 4)) * 5)

    @patch('src.recommendation.bigquery_utils.client')
    def test_run_dry_run_failure(self, mock_client):
        mock_client.query.side_effect = Exception("query failed")

        from src.recommendation.bigquery_utils import run_dry_run
        bytes_processed, cost = run_dry_run("INVALID SQL")

        self.assertIsNone(bytes_processed)
        self.assertIsNone(cost)

    @patch('src.recommendation.bigquery_utils.client')
    def test_create_bq_table_if_not_exists_exists(self, mock_client):
        # Simulate table exists (no exception)
        mock_client.get_table.return_value = MagicMock()

        from src.recommendation.bigquery_utils import create_bq_table_if_not_exists
        create_bq_table_if_not_exists('greenquery_core', 'dryrun_analysis')

        mock_client.get_table.assert_called_once()
        mock_client.create_table.assert_not_called()

    @patch('src.recommendation.bigquery_utils.client')
    def test_create_bq_table_if_not_exists_missing(self, mock_client):
        # Simulate table does not exist (raises exception)
        mock_client.get_table.side_effect = Exception("Table not found")
        mock_client.create_table.return_value = None

        from src.recommendation.bigquery_utils import create_bq_table_if_not_exists
        create_bq_table_if_not_exists('greenquery_core', 'dryrun_analysis')

        mock_client.get_table.assert_called_once()
        mock_client.create_table.assert_called_once()

    @patch('src.recommendation.bigquery_utils.client')
    def test_merge_staging_into_target(self, mock_client):
        mock_job = MagicMock()
        mock_job.result.return_value = None
        mock_client.query.return_value = mock_job

        from src.recommendation.bigquery_utils import merge_staging_into_target
        merge_staging_into_target('greenquery_core', 'dryrun_analysis', 'dryrun_analysis_staging')

        mock_client.query.assert_called_once()
        mock_job.result.assert_called_once()

    @patch('src.recommendation.bigquery_utils.client')
    def test_load_dataframe_to_bq_upsert_table_exists(self, mock_client):
        # Table exists - no creation
        mock_client.get_table.return_value = MagicMock()

        # Mock truncate query job
        mock_truncate_job = MagicMock()
        mock_truncate_job.result.return_value = None
        mock_client.query.return_value = mock_truncate_job

        # Mock load job
        mock_load_job = MagicMock()
        mock_load_job.output_rows = 42
        mock_load_job.result.return_value = None
        mock_client.load_table_from_dataframe.return_value = mock_load_job

        from src.recommendation.bigquery_utils import load_dataframe_to_bq_upsert

        df = pd.DataFrame({
            "record_hash": ["rec1"],
            "file": ["file.sql"],
            "processed_gb": [1.23],
            "estimated_cost_usd": [0.45],
            "estimated_carbon_kg": [0.006],
            "recommendations": [["rec"]],
            "load_time_utc": [pd.Timestamp("2025-07-23T19:41:00")]
        })

        load_dataframe_to_bq_upsert(df, 'greenquery_core', 'dryrun_analysis')

        mock_client.get_table.assert_called_once()
        mock_client.query.assert_called()  # truncate query
        mock_client.load_table_from_dataframe.assert_called_once()

    @patch('src.recommendation.bigquery_utils.client')
    def test_load_dataframe_to_bq_upsert_table_missing(self, mock_client):
        # Table does not exist, triggers creation
        mock_client.get_table.side_effect = Exception("Table not found")
        mock_client.create_table.return_value = None

        mock_load_job = MagicMock()
        mock_load_job.output_rows = 3
        mock_load_job.result.return_value = None
        mock_client.load_table_from_dataframe.return_value = mock_load_job

        from src.recommendation.bigquery_utils import load_dataframe_to_bq_upsert

        df = pd.DataFrame({
            "record_hash": ["rec2"],
            "file": ["file2.sql"],
            "processed_gb": [4.56],
            "estimated_cost_usd": [1.23],
            "estimated_carbon_kg": [0.012],
            "recommendations": [["rec"]],
            "load_time_utc": [pd.Timestamp("2025-07-23T19:41:00")]
        })

        load_dataframe_to_bq_upsert(df, 'greenquery_core', 'dryrun_analysis')

        mock_client.get_table.assert_called_once()
        mock_client.create_table.assert_called_once()
        mock_client.load_table_from_dataframe.assert_called_once()


if __name__ == '__main__':
    unittest.main()
