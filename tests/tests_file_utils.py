import unittest
import tempfile
import os

from src.recommendation.file_utils import read_sql_file, split_sql_statements, classify_sql_statements

class TestFileUtils(unittest.TestCase):
    def test_read_sql_file(self):
        # Writes a tmp file, reads back with util
        with tempfile.NamedTemporaryFile(mode='w+', delete=False) as tf:
            tf.write("SELECT 1;")
            tf.flush()
            tf.close()
            content = read_sql_file(tf.name)
        self.assertEqual(content, "SELECT 1;")
        os.unlink(tf.name)

    def test_split_sql_statements(self):
        sql = "SELECT 1; SELECT 2 ;SELECT 3;"
        stmts = split_sql_statements(sql)
        self.assertEqual(stmts, ["SELECT 1", "SELECT 2", "SELECT 3"])

    def test_classify_sql_statements(self):
        sql = (
            "SELECT 1;\n"
            "INSERT INTO t VALUES(1);\n"
            "CREATE TABLE x(y INT);\n"
            "WITH q AS (SELECT 2) SELECT * FROM q;\n"
            "WITH c1 AS (SELECT 1), c2 AS (SELECT 2) INSERT INTO x SELECT * FROM c1;"
        )
        classified = classify_sql_statements(sql)
        self.assertEqual(len(classified['select']), 3)   # Actual classified SELECTs
        self.assertEqual(len(classified['insert']), 1)   # Only one INSERT detected
        self.assertEqual(len(classified['create']), 1)   # One CREATE
        self.assertEqual(len(classified['other']), 0)    # No 'other'

