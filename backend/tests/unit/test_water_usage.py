import unittest
from datetime import date
from src.scd2_utils import get_rate, get_emission_factor

mock_data = [
    {
        "category": "water_usage",
        "sub_category": "Supply",
        "currency": "GBP",
        "rate_per_unit": 1.0,
        "unit": "unit",
        "kg_co2e_per_unit": 2.0,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "water_usage",
        "sub_category": "Treatment",
        "currency": "GBP",
        "rate_per_unit": 1.1,
        "unit": "unit",
        "kg_co2e_per_unit": 2.1,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    }
]

class TestWaterUsage(unittest.TestCase):

    def test_rate_supply(self):
        rate = get_rate(mock_data, "water_usage", "Supply", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.0)

    def test_emission_supply(self):
        ef = get_emission_factor(mock_data, "water_usage", "Supply", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.0)

    def test_rate_treatment(self):
        rate = get_rate(mock_data, "water_usage", "Treatment", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.1)

    def test_emission_treatment(self):
        ef = get_emission_factor(mock_data, "water_usage", "Treatment", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.1)


if __name__ == "__main__":
    unittest.main()
