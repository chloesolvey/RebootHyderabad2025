import unittest
from datetime import date
from src.scd2_utils import get_rate, get_emission_factor

mock_data = [
    {
        "category": "electricity_usage",
        "sub_category": "Residential",
        "currency": "GBP",
        "rate_per_unit": 1.0,
        "unit": "unit",
        "kg_co2e_per_unit": 2.0,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "electricity_usage",
        "sub_category": "Commercial",
        "currency": "GBP",
        "rate_per_unit": 1.1,
        "unit": "unit",
        "kg_co2e_per_unit": 2.1,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    }
]

class TestElectricityUsage(unittest.TestCase):

    def test_rate_residential(self):
        rate = get_rate(mock_data, "electricity_usage", "Residential", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.0)

    def test_emission_residential(self):
        ef = get_emission_factor(mock_data, "electricity_usage", "Residential", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.0)

    def test_rate_commercial(self):
        rate = get_rate(mock_data, "electricity_usage", "Commercial", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.1)

    def test_emission_commercial(self):
        ef = get_emission_factor(mock_data, "electricity_usage", "Commercial", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.1)


if __name__ == "__main__":
    unittest.main()
