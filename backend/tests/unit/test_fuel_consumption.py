import unittest
from datetime import date
from src.scd2_utils import get_rate, get_emission_factor

mock_data = [
    {
        "category": "fuel_consumption",
        "sub_category": "Petrol",
        "currency": "GBP",
        "rate_per_unit": 1.0,
        "unit": "unit",
        "kg_co2e_per_unit": 2.0,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "fuel_consumption",
        "sub_category": "Diesel",
        "currency": "GBP",
        "rate_per_unit": 1.1,
        "unit": "unit",
        "kg_co2e_per_unit": 2.1,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "fuel_consumption",
        "sub_category": "LPG",
        "currency": "GBP",
        "rate_per_unit": 1.2,
        "unit": "unit",
        "kg_co2e_per_unit": 2.2,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "fuel_consumption",
        "sub_category": "CNG",
        "currency": "GBP",
        "rate_per_unit": 1.3,
        "unit": "unit",
        "kg_co2e_per_unit": 2.3,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    }
]

class TestFuelConsumption(unittest.TestCase):

    def test_rate_petrol(self):
        rate = get_rate(mock_data, "fuel_consumption", "Petrol", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.0)

    def test_emission_petrol(self):
        ef = get_emission_factor(mock_data, "fuel_consumption", "Petrol", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.0)

    def test_rate_diesel(self):
        rate = get_rate(mock_data, "fuel_consumption", "Diesel", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.1)

    def test_emission_diesel(self):
        ef = get_emission_factor(mock_data, "fuel_consumption", "Diesel", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.1)

    def test_rate_lpg(self):
        rate = get_rate(mock_data, "fuel_consumption", "LPG", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.2)

    def test_emission_lpg(self):
        ef = get_emission_factor(mock_data, "fuel_consumption", "LPG", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.2)

    def test_rate_cng(self):
        rate = get_rate(mock_data, "fuel_consumption", "CNG", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.3)

    def test_emission_cng(self):
        ef = get_emission_factor(mock_data, "fuel_consumption", "CNG", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.3)


if __name__ == "__main__":
    unittest.main()
