def get_rate(table, category, sub_category, currency, query_date):
    for row in table:
        if (row['category'] == category and row['sub_category'] == sub_category and row['currency'] == currency and
            row['effective_from_date'] <= query_date <= row['effective_to_date']):
            return row['rate_per_unit']
    return None

def get_emission_factor(table, activity_type, sub_activity, unit, query_date):
    for row in table:
        if (row['activity_type'] == activity_type and row['sub_activity'] == sub_activity and row['unit'] == unit and
            row['effective_from_date'] <= query_date <= row['effective_to_date']):
            return row['kg_co2e_per_unit']
    return None
