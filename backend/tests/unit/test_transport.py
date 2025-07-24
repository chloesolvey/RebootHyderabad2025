import unittest
from datetime import date
from src.scd2_utils import get_rate, get_emission_factor

mock_data = [
    {
        "category": "transport",
        "sub_category": "Bus",
        "currency": "GBP",
        "rate_per_unit": 1.0,
        "unit": "unit",
        "kg_co2e_per_unit": 2.0,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "transport",
        "sub_category": "Car",
        "currency": "GBP",
        "rate_per_unit": 1.1,
        "unit": "unit",
        "kg_co2e_per_unit": 2.1,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "transport",
        "sub_category": "Train",
        "currency": "GBP",
        "rate_per_unit": 1.2,
        "unit": "unit",
        "kg_co2e_per_unit": 2.2,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "transport",
        "sub_category": "Taxi",
        "currency": "GBP",
        "rate_per_unit": 1.3,
        "unit": "unit",
        "kg_co2e_per_unit": 2.3,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    }
]

class TestTransport(unittest.TestCase):

    def test_rate_bus(self):
        rate = get_rate(mock_data, "transport", "Bus", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.0)

    def test_emission_bus(self):
        ef = get_emission_factor(mock_data, "transport", "Bus", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.0)

    def test_rate_car(self):
        rate = get_rate(mock_data, "transport", "Car", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.1)

    def test_emission_car(self):
        ef = get_emission_factor(mock_data, "transport", "Car", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.1)

    def test_rate_train(self):
        rate = get_rate(mock_data, "transport", "Train", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.2)

    def test_emission_train(self):
        ef = get_emission_factor(mock_data, "transport", "Train", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.2)

    def test_rate_taxi(self):
        rate = get_rate(mock_data, "transport", "Taxi", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.3)

    def test_emission_taxi(self):
        ef = get_emission_factor(mock_data, "transport", "Taxi", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.3)


if __name__ == "__main__":
    unittest.main()
