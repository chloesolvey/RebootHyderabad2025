import unittest
from datetime import date
from src.scd2_utils import get_rate, get_emission_factor

mock_data = [
    {
        "category": "waste_disposal",
        "sub_category": "Landfill",
        "currency": "GBP",
        "rate_per_unit": 1.0,
        "unit": "unit",
        "kg_co2e_per_unit": 2.0,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "waste_disposal",
        "sub_category": "Recycling",
        "currency": "GBP",
        "rate_per_unit": 1.1,
        "unit": "unit",
        "kg_co2e_per_unit": 2.1,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    },
    {
        "category": "waste_disposal",
        "sub_category": "Compost",
        "currency": "GBP",
        "rate_per_unit": 1.2,
        "unit": "unit",
        "kg_co2e_per_unit": 2.2,
        "effective_from_date": date(2024, 1, 1),
        "effective_to_date": date(9999, 12, 31)
    }
]

class TestWasteDisposal(unittest.TestCase):

    def test_rate_landfill(self):
        rate = get_rate(mock_data, "waste_disposal", "Landfill", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.0)

    def test_emission_landfill(self):
        ef = get_emission_factor(mock_data, "waste_disposal", "Landfill", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.0)

    def test_rate_recycling(self):
        rate = get_rate(mock_data, "waste_disposal", "Recycling", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.1)

    def test_emission_recycling(self):
        ef = get_emission_factor(mock_data, "waste_disposal", "Recycling", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.1)

    def test_rate_compost(self):
        rate = get_rate(mock_data, "waste_disposal", "Compost", "GBP", date(2025, 6, 1))
        self.assertEqual(rate, 1.2)

    def test_emission_compost(self):
        ef = get_emission_factor(mock_data, "waste_disposal", "Compost", "unit", date(2025, 6, 1))
        self.assertEqual(ef, 2.2)


if __name__ == "__main__":
    unittest.main()
