CREATE SCHEMA IF NOT EXISTS `iconic-iridium-463506-v6.cloudcompass_scd2_composer`
OPTIONS(
location = 'europe-west2'
);

-- 2.1. Transaction_Table
CREATE TABLE IF NOT EXISTS `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transactions` (
    transaction_id STRING NOT NULL OPTIONS(description="Unique identifier for the bank transaction."),
    user_id STRING NOT NULL OPTIONS(description="Unique identifier for the individual user."),
    transaction_date DATE NOT NULL OPTIONS(description="The date on which the activity occurred."),
    transaction_description STRING NOT NULL OPTIONS(description="Description of the bank transaction, used to infer activity_category,activity_subcategory, and activity_type."),
    transaction_amount NUMERIC NOT NULL OPTIONS(description="The monetary amount of the transaction."),
    currency STRING NOT NULL OPTIONS(description="The currency used in the transaction (e.g., 'GBP', 'USD')."),
    ingestion_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP() OPTIONS(description="Timestamp when the transaction was ingested.")
)
PARTITION BY transaction_date
CLUSTER BY user_id, ingestion_timestamp
OPTIONS(
    description="Stores raw bank transaction details for each individual, enhanced with currency and ingestion timestamp."
);

-- 2.2. Conversion_Table
CREATE TABLE IF NOT EXISTS `iconic-iridium-463506-v6.cloudcompass_scd2_composer.conversions` (
    conversion_id STRING NOT NULL OPTIONS(description="Unique identifier for the logical conversion rule."),
    category STRING NOT NULL OPTIONS(description="The type of expense (e.g., 'Petrol', 'Diesel', 'Electricity')."),
    currency STRING NOT NULL OPTIONS(description="The currency used in the transaction (e.g., 'GBP', 'USD')."),
    unit STRING NOT NULL OPTIONS(description="The physical unit for conversion (e.g., 'Liter', 'Gallon', 'KWH')."),
    rate_per_unit NUMERIC NOT NULL OPTIONS(description="The monetary amount per unit (e.g., 1.60 for 1.60 GBP per Liter)."),
    year INT64 NOT NULL OPTIONS(description="The year for which the conversion rate is valid."),
    effective_from_date DATE NOT NULL OPTIONS(description="The date from which this version of the record is effective."),
    effective_to_date DATE NOT NULL OPTIONS(description="The date until which this version of the record is effective (inclusive). '9999-12-31' for current records."),
    is_current BOOLEAN NOT NULL OPTIONS(description="Flag indicating if this is the current active version of the record.")
)
-- Clustering by natural key for efficient SCD2 lookups and merges
CLUSTER BY conversion_id, category, currency, unit
OPTIONS(
    description="Lookup table to convert monetary transaction amounts into physical quantities, supporting SCD Type 2 for historical changes."
);

-- 2.3. Emission_Factor_Table
CREATE TABLE IF NOT EXISTS `iconic-iridium-463506-v6.cloudcompass_scd2_composer.emission_factors` (
    emission_factor_id STRING NOT NULL OPTIONS(description="Unique identifier for the logical emission factor rule."),
    activity_category STRING NOT NULL OPTIONS(description="Broad category of the activity (e.g., Transportation)."),
    activity_subcategory STRING NOT NULL OPTIONS(description="More specific type of activity within the category (e.g., Car, Motorbike, EV Electricity)."),
    activity_type STRING NOT NULL OPTIONS(description="Detailed description of the activity (e.g., Diesel Supermini, Average, Supermini (BEV))."),
    unit STRING NOT NULL OPTIONS(description="Unit of measurement for the activity data (e.g., km)."),
    kg_co2e_per_unit NUMERIC NOT NULL OPTIONS(description="The conversion factor in kg CO2e per unit of activity."),
    emission_scope STRING NOT NULL OPTIONS(description="The emission scope (e.g., Scope 1, Scope 2, Scope 3)."),
    year INT64 NOT NULL OPTIONS(description="The year for which the emission factor is valid."),
    effective_from_date DATE NOT NULL OPTIONS(description="The date from which this version of the record is effective."),
    effective_to_date DATE NOT NULL OPTIONS(description="The date until which this version of the record is effective (inclusive). '9999-12-31' for current records."),
    is_current BOOLEAN NOT NULL OPTIONS(description="Flag indicating if this is the current active version of the record.")
)
-- Clustering by natural key for efficient SCD2 lookups and merges
CLUSTER BY activity_category, activity_subcategory, activity_type, unit
OPTIONS(
    description="Lookup table storing conversion factors from physical activity units to kilograms of CO2 equivalent (kg CO2e), supporting SCD Type 2."
);

-- 2.4. Transaction_Activity_Mapping_Table (New and Crucial Table)
CREATE TABLE IF NOT EXISTS `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transaction_activity_mappings` (
    mapping_id STRING NOT NULL OPTIONS(description="Unique identifier for the logical mapping rule."),
    transaction_keyword_pattern STRING NOT NULL OPTIONS(description="A keyword or regular expression pattern to match transaction_description. Consider using REGEXP functions in actual matching."),
    mapped_activity_category STRING NOT NULL OPTIONS(description="Corresponds to Emission_Factor.Activity_Category."),
    mapped_activity_subcategory STRING NOT NULL OPTIONS(description="Corresponds to Emission_Factor.Activity_Subcategory."),
    mapped_activity_type STRING NOT NULL OPTIONS(description="Corresponds to Emission_Factor.Activity_Type."),
    final_activity_unit STRING NOT NULL OPTIONS(description="The target unit for activity_quantity that matches Emission_Factor.Unit."),
    quantity_derivation_method STRING NOT NULL OPTIONS(description="Defines how activity_quantity is derived ('MonetaryConversion', 'FixedQuantity', 'EstimateFromAmount')."),
    conversion_table_category_key STRING OPTIONS(description="If MonetaryConversion, this links to Conversion_Table.category."),
    fixed_quantity_value NUMERIC OPTIONS(description="If FixedQuantity, this is the value (e.g., 1 for a hotel night)."),
    estimated_cost_per_activity_unit NUMERIC OPTIONS(description="If EstimateFromAmount, this is the average cost per final_activity_unit (e.g., average GBP per km for a taxi)."),
    multi_scope_output_flag BOOLEAN NOT NULL OPTIONS(description="Indicates if a single transaction mapping might result in multiple emission entries."),
    notes STRING OPTIONS(description="Any specific notes or assumptions for the mapping rule."),
    effective_from_date DATE NOT NULL OPTIONS(description="The date from which this version of the record is effective."),
    effective_to_date DATE NOT NULL OPTIONS(description="The date until which this version of the record is effective (inclusive). '9999-12-31' for current records."),
    is_current BOOLEAN NOT NULL OPTIONS(description="Flag indicating if this is the current active version of the record.")
)
-- Clustering by natural key for efficient SCD2 lookups and merges
CLUSTER BY mapping_id, mapped_activity_category, mapped_activity_subcategory, mapped_activity_type
OPTIONS(
    description="Crucial lookup table to translate unstructured transaction descriptions into standardized activity types and specify quantity derivation logic, supporting SCD Type 2."
);

-- 2.5. Final_Carbon_Footprint_Table (Output Table)

CREATE TABLE IF NOT EXISTS `iconic-iridium-463506-v6.cloudcompass_scd2_composer.final_carbon_footprint` (
    carbon_footprint_id STRING NOT NULL OPTIONS(description="Unique identifier for each calculated carbon footprint entry."),
    activity_date DATE NOT NULL OPTIONS(description="The date on which the activity occurred."),
    user_id STRING NOT NULL OPTIONS(description="Unique identifier for the individual user."),
    transaction_id STRING NOT NULL OPTIONS(description="Unique identifier for the bank transaction."),
    transaction_description STRING NOT NULL OPTIONS(description="Description of the bank transaction, used to infer activity types."),
    transaction_amount NUMERIC NOT NULL OPTIONS(description="The monetary amount of the transaction."),
    activity_category STRING NOT NULL OPTIONS(description="Broad category of the activity (e.g., Home Energy, Transportation)."),
    activity_subcategory STRING NOT NULL OPTIONS(description="More specific type of activity within the category (e.g., Electricity, Car)."),
    activity_type STRING NOT NULL OPTIONS(description="Detailed description of the activity (e.g., UK Grid, Diesel)."),
    activity_unit STRING NOT NULL OPTIONS(description="Unit of measurement for the activity data (e.g., kWh, km)."),
    activity_quantity NUMERIC NOT NULL OPTIONS(description="The measured quantity of the activity, derived from transaction data."),
    emission_factor_kg_co2e_per_unit NUMERIC NOT NULL OPTIONS(description="The conversion factor in kg CO2e per unit of activity."),
    emission_scope STRING NOT NULL OPTIONS(description="The emission scope (Scope 1, Scope 2, or Scope 3)."),
    calculated_kg_co2e NUMERIC NOT NULL OPTIONS(description="The calculated GHG emissions in kg CO2e for this activity (activity_quantity * emission_factor_kg_co2e_per_unit)."),
    data_source STRING OPTIONS(description="Original source of the emission factor (e.g., UK Gov GHG Factors 2025)."),
    notes STRING OPTIONS(description="Any relevant notes or assumptions for the activity or factor."),
    processed_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP() OPTIONS(description="Timestamp when this carbon footprint record was processed.")
)
PARTITION BY activity_date
CLUSTER BY user_id, activity_category, emission_scope, processed_timestamp
OPTIONS(
    description="Stores the comprehensive, calculated carbon footprint data for analysis and reporting, append-only to preserve history."
);

-- 3.1. Insert Sample Data into Conversion_Table
INSERT INTO `iconic-iridium-463506-v6.cloudcompass_scd2_composer.conversions` (conversion_id, category, currency, unit, rate_per_unit, year, effective_from_date, effective_to_date, is_current)
VALUES
    ('petrol_gbp_liter_2025', 'Petrol', 'GBP', 'liter', 1.60, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('diesel_gbp_liter_2025', 'Diesel', 'GBP', 'liter', 1.70, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('electricity_gbp_kwh_2025', 'Electricity', 'GBP', 'KWH', 0.30, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('natural_gas_gbp_kwh_2025', 'Natural Gas', 'GBP', 'KWH', 0.08, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('lpg_gbp_kg_2025', 'LPG', 'GBP', 'kg', 0.80, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('coal_gbp_kg_2025', 'Coal', 'GBP', 'kg', 0.50, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('wood_gbp_kg_2025', 'Wood', 'GBP', 'kg', 0.20, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('water_gbp_m3_2025', 'Water', 'GBP', 'm3', 2.00, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('taxi_gbp_km_2025', 'Taxi', 'GBP', 'passenger.km', 1.50, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('bus_gbp_km_2025', 'Bus', 'GBP', 'passenger.km', 0.20, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('rail_gbp_km_2025', 'Rail', 'GBP', 'passenger.km', 0.35, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('air_travel_domestic_gbp_km_2025', 'Air Travel', 'GBP', 'passenger.km', 0.50, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('air_travel_longhaul_gbp_km_2025', 'Air Travel', 'GBP', 'passenger.km', 0.30, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('sea_travel_ferry_gbp_km_2025', 'Sea Travel', 'GBP', 'passenger.km', 0.05, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('hotel_gbp_night_2025', 'Hotel', 'GBP', 'room_per_night', 100.00, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('food_gbp_meal_2025', 'Food', 'GBP', 'meal', 15.00, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('clothing_gbp_item_2025', 'Clothing', 'GBP', 'item', 30.00, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('electronic_gbp_item_2025', 'Electronics', 'GBP', 'item', 200.00, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('waste_mixed_recyclable_gbp_kg_2025', 'Waste', 'GBP', 'kg', 0.10, 2025, '2025-02-01', '9999-12-31', TRUE),
    ('waste_landfill_gbp_kg_2025', 'Waste', 'GBP', 'kg', 0.25, 2025, '2025-02-01', '9999-12-31', TRUE);

-- 3.2. Initial Insert Sample Data into Emission_Factor_Table (effective from 2025-02-01)
INSERT INTO `iconic-iridium-463506-v6.cloudcompass_scd2_composer.emission_factors` (emission_factor_id, activity_category, activity_subcategory, activity_type, unit, kg_co2e_per_unit, emission_scope, year, effective_from_date, effective_to_date, is_current)
VALUES
    ('car_petrol_supermini_litres_scope1_2025', 'Transportation', 'Car', 'Petrol (average biofuel blend)', 'litres', 2.06916, 'Scope 1', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('car_diesel_supermini_litres_scope1_2025', 'Transportation', 'Car', 'Diesel (average biofuel blend)', 'litres', 2.65171, 'Scope 1', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('taxi_regular_pkm_scope3_2025', 'Transportation', 'Taxi', 'Regular taxi', 'passenger.km', 0.14861, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('bus_local_london_pkm_scope3_2025', 'Transportation', 'Bus', 'Local London bus', 'passenger.km', 0.06875, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('rail_national_pkm_scope3_2025', 'Transportation', 'Rail', 'National rail', 'passenger.km', 0.03546, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('motorbike_average_pkm_scope1_2025', 'Transportation', 'Motorbike', 'Average', 'km', 0.11367, 'Scope 1', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('ev_electricity_supermini_km_scope2_2025', 'Transportation', 'EV Electricity', 'Supermini (BEV)', 'km', 0.03419, 'Scope 2', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('ev_tnd_loss_supermini_km_scope3_2025', 'Transportation', 'EV T&D Losses', 'Supermini (BEV)', 'km', 0.00357, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('air_domestic_avg_pkm_scope3_2025', 'Transportation', 'Air Travel', 'Domestic (Average passenger, With RF)', 'passenger.km', 0.22928, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('air_longhaul_econ_pkm_scope3_2025', 'Transportation', 'Air Travel', 'Long-haul Economy (With RF)', 'passenger.km', 0.11704, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('sea_ferry_foot_pkm_scope3_2025', 'Transportation', 'Sea Travel', 'Ferry (Foot passenger)', 'passenger.km', 0.01871, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('uk_grid_electricity_kwh_scope2_2025', 'Home Energy', 'Electricity', 'UK Grid', 'KWH', 0.177, 'Scope 2', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('natural_gas_kwh_scope1_2025', 'Home Energy', 'Natural Gas', 'Average', 'KWH', 0.18388, 'Scope 1', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('lpg_kilograms_scope1_2025', 'Home Energy', 'LPG', 'Average', 'kg', 2.983, 'Scope 1', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('coal_kilograms_scope1_2025', 'Home Energy', 'Coal', 'Average', 'kg', 2.378, 'Scope 1', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('wood_kilograms_scope1_2025', 'Home Energy', 'Bioenergy', 'Wood', 'kg', 0.012, 'Scope 1', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('hotel_uk_room_night_scope3_2025', 'Lifestyle', 'Hotel stay', 'Average UK hotel', 'room_per_night', 10.4, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('food_average_meal_scope3_2025', 'Consumption', 'Food', 'Average meal', 'meal', 1.2, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('clothing_textile_item_scope3_2025', 'Consumption', 'Material Use', 'Textile: clothing (Primary production)', 'item', 15.0, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('electronic_equipment_item_scope3_2025', 'Consumption', 'Material Use', 'Electronic equipment', 'item', 25.0, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('waste_mixed_recyclable_kg_scope3_2025', 'Waste', 'Waste disposal', 'Mixed recyclable waste', 'kg', 0.05, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('waste_landfill_kg_scope3_2025', 'Waste', 'Waste disposal', 'Landfill (mixed waste)', 'kg', 0.4, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('water_supply_average_m3_scope3_2025', 'Utilities', 'Water supply', 'Average', 'm3', 0.344, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE),
    ('water_treatment_average_m3_scope3_2025', 'Utilities', 'Water treatment', 'Average', 'm3', 0.176, 'Scope 3', 2025, '2025-02-01', '9999-12-31', TRUE);

-- 3.3. Initial Insert Sample Data into Transaction_Activity_Mapping_Table (effective from 2025-02-01)
INSERT INTO `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transaction_activity_mappings` (
    mapping_id, transaction_keyword_pattern, mapped_activity_category, mapped_activity_subcategory, mapped_activity_type,
    final_activity_unit, quantity_derivation_method, conversion_table_category_key, fixed_quantity_value,
    estimated_cost_per_activity_unit, multi_scope_output_flag, notes, effective_from_date, effective_to_date, is_current
)
VALUES
    ('map_petrol_station', 'Petrol Station|BP Garage|Shell|Esso Fuel', 'Transportation', 'Car', 'Petrol (average biofuel blend)', 'litres', 'MonetaryConversion', 'Petrol', NULL, NULL, FALSE, 'Assumes average car type and biofuel blend', '2025-02-01', '9999-12-31', TRUE),
    ('map_diesel_station', 'Diesel Station|Esso Diesel|Texaco Diesel', 'Transportation', 'Car', 'Diesel (average biofuel blend)', 'litres', 'MonetaryConversion', 'Diesel', NULL, NULL, FALSE, 'Assumes average car type and biofuel blend', '2025-02-01', '9999-12-31', TRUE),
    ('map_motorbike_fuel', 'Motorbike Fuel|Bike Fuel', 'Transportation', 'Motorbike', 'Average', 'km', 'EstimateFromAmount', NULL, NULL, 0.50, FALSE, 'Estimated km from motorbike fuel spend', '2025-02-01', '9999-12-31', TRUE),
    ('map_ev_charge_home', 'EV Charge - Home|Electric Car Bill|Home EV Charging', 'Transportation', 'EV Electricity', 'Supermini (BEV)', 'km', 'EstimateFromAmount', NULL, NULL, 0.15, TRUE, 'Home EV charging - triggers multi-scope output (Scope 2 & Scope 3 T&D)', '2025-02-01', '9999-12-31', TRUE),
    ('map_ev_charge_public', 'Public EV Charge|ChargePoint|Ionity Charger', 'Transportation', 'EV Electricity', 'Supermini (BEV)', 'km', 'EstimateFromAmount', NULL, NULL, 0.25, TRUE, 'Public EV charging - triggers multi-scope output (Scope 2 & Scope 3 T&D)', '2025-02-01', '9999-12-31', TRUE),
    ('map_electric_bill', 'Electricity Bill|Utility Co. Electric|Power Bill', 'Home Energy', 'Electricity', 'UK Grid', 'KWH', 'MonetaryConversion', 'Electricity', NULL, NULL, FALSE, 'Home electricity consumption', '2025-02-01', '9999-12-31', TRUE),
    ('map_gas_bill', 'Gas Bill|Utility Co. Gas|Heating Bill', 'Home Energy', 'Natural Gas', 'Average', 'KWH', 'MonetaryConversion', 'Natural Gas', NULL, NULL, FALSE, 'Home natural gas consumption', '2025-02-01', '9999-12-31', TRUE),
    ('map_lpg_delivery', 'LPG Delivery|Gas Bottle Refill', 'Home Energy', 'LPG', 'Average', 'kg', 'MonetaryConversion', 'LPG', NULL, NULL, FALSE, 'LPG purchase for heating/cooking', '2025-02-01', '9999-12-31', TRUE),
    ('map_coal_purchase', 'Coal Merchant|Solid Fuel', 'Home Energy', 'Coal', 'Average', 'kg', 'MonetaryConversion', 'Coal', NULL, NULL, FALSE, 'Coal purchase for heating', '2025-02-01', '9999-12-31', TRUE),
    ('map_wood_fuel', 'Firewood|Wood Logs', 'Home Energy', 'Bioenergy', 'Wood', 'kg', 'MonetaryConversion', 'Wood', NULL, NULL, FALSE, 'Wood fuel purchase', '2025-02-01', '9999-12-31', TRUE),
    ('map_water_bill', 'Water Bill|Water Services|Thames Water', 'Utilities', 'Water supply', 'Average', 'm3', 'MonetaryConversion', 'Water', NULL, NULL, TRUE, 'Water supply bill - potentially triggers water supply and treatment', '2025-02-01', '9999-12-31', TRUE),
    ('map_taxi', 'Uber|Taxi|Cab|Local Cabs|Bolt Ride', 'Transportation', 'Taxi', 'Regular taxi', 'passenger.km', 'EstimateFromAmount', NULL, NULL, 1.50, FALSE, 'Estimated 1.50 GBP/km for taxi rides', '2025-02-01', '9999-12-31', TRUE),
    ('map_bus_fare', 'Bus Ticket|Transport for London Bus|Stagecoach Bus', 'Transportation', 'Bus', 'Local London bus', 'passenger.km', 'EstimateFromAmount', NULL, NULL, 0.20, FALSE, 'Estimated 0.20 GBP/km for bus rides', '2025-02-01', '9999-12-31', TRUE),
    ('map_train_ticket', 'Train Ticket|National Rail|Trainline|Avanti West Coast', 'Transportation', 'Rail', 'National rail', 'passenger.km', 'EstimateFromAmount', NULL, NULL, 0.35, FALSE, 'Estimated 0.35 GBP/km for train rides', '2025-02-01', '9999-12-31', TRUE),
    ('map_air_domestic', 'Flight Ticket Domestic|EasyJet UK|BA Domestic', 'Transportation', 'Air Travel', 'Domestic (Average passenger, With RF)', 'passenger.km', 'EstimateFromAmount', NULL, NULL, 0.50, FALSE, 'Estimated 0.50 GBP/km for domestic air travel', '2025-02-01', '9999-12-31', TRUE),
    ('map_air_longhaul', 'Flight Ticket Longhaul|Virgin Atlantic|Emirates Flight', 'Transportation', 'Air Travel', 'Long-haul Economy (With RF)', 'passenger.km', 'EstimateFromAmount', NULL, NULL, 0.30, FALSE, 'Estimated 0.30 GBP/km for long-haul air travel', '2025-02-01', '9999-12-31', TRUE),
    ('map_ferry_travel', 'Ferry Ticket|DFDS Ferry', 'Transportation', 'Sea Travel', 'Ferry (Foot passenger)', 'passenger.km', 'EstimateFromAmount', NULL, NULL, 0.05, FALSE, 'Estimated 0.05 GBP/km for ferry travel', '2025-02-01', '9999-12-31', TRUE),
    ('map_hotel', 'Hotel|Travelodge|Premier Inn|Hilton Stay', 'Lifestyle', 'Hotel stay', 'Average UK hotel', 'room_per_night', 'FixedQuantity', NULL, 1.0, NULL, FALSE, 'Assumes one hotel night per transaction', '2025-02-01', '9999-12-31', TRUE),
    ('map_groceries', 'Supermarket|Tesco|Sainsburys|Aldi|Groceries|Morrisons', 'Consumption', 'Food', 'Average meal', 'meal', 'EstimateFromAmount', NULL, NULL, 15.00, FALSE, 'Estimates number of meals from grocery spend', '2025-02-01', '9999-12-31', TRUE),
    ('map_clothing', 'Fashion|Zara|H&M|Clothing Store|Next Shop', 'Consumption', 'Material Use', 'Textile: clothing (Primary production)', 'item', 'EstimateFromAmount', NULL, NULL, 30.00, FALSE, 'Estimates number of clothing items from spend', '2025-02-01', '9999-12-31', TRUE),
    ('map_electronics', 'Currys|PC World|Tech Shop|Apple Store', 'Consumption', 'Material Use', 'Electronic equipment', 'item', 'EstimateFromAmount', NULL, NULL, 200.00, FALSE, 'Estimates number of electronic items from spend', '2025-02-01', '9999-12-31', TRUE),
    ('map_recycling_centre', 'Recycling Centre Fee|Waste Disposal Site', 'Waste', 'Waste disposal', 'Mixed recyclable waste', 'kg', 'MonetaryConversion', 'Waste (Mixed Recyclable)', NULL, NULL, FALSE, 'Fee for depositing recyclable waste', '2025-02-01', '9999-12-31', TRUE),
    ('map_skip_hire', 'Skip Hire|Waste Removal', 'Waste', 'Waste disposal', 'Landfill (mixed waste)', 'kg', 'MonetaryConversion', 'Waste (Landfill)', NULL, NULL, FALSE, 'Fee for waste going to landfill', '2025-02-01', '9999-12-31', TRUE);

-- 3.4. Insert Sample Data into Transaction_Table (6 months, 15 users, simulated ingestion_timestamp)
-- For a real pipeline, ingestion_timestamp would be CURRENT_TIMESTAMP() when data arrives.
-- Here, we backdate it to simulate daily ingestion over the 6 months.
INSERT INTO `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transactions` (transaction_id, user_id, transaction_date, transaction_description, transaction_amount, currency, ingestion_timestamp)
VALUES
    -- User_A (Mixed transportation, utilities, groceries) - Feb-Jul
    (GENERATE_UUID(), 'user_A', '2025-02-05', 'BP Garage Refuel', 60.00, 'GBP', TIMESTAMP('2025-02-05 10:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-02-15', 'Tesco Superstore', 35.50, 'GBP', TIMESTAMP('2025-02-15 11:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-02-28', 'Electricity Bill Feb', 80.00, 'GBP', TIMESTAMP('2025-02-28 09:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-03-10', 'Uber Ride London', 22.00, 'GBP', TIMESTAMP('2025-03-10 14:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-03-20', 'Sainsburys Local', 78.90, 'GBP', TIMESTAMP('2025-03-20 16:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-03-31', 'Gas Bill Mar', 55.00, 'GBP', TIMESTAMP('2025-03-31 10:30:00')),
    (GENERATE_UUID(), 'user_A', '2025-04-03', 'Shell Garage Refuel', 65.00, 'GBP', TIMESTAMP('2025-04-03 12:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-04-12', 'Aldi Groceries', 42.10, 'GBP', TIMESTAMP('2025-04-12 13:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-04-25', 'Local Cabs', 15.00, 'GBP', TIMESTAMP('2025-04-25 15:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-05-08', 'Electricity Bill Apr', 85.00, 'GBP', TIMESTAMP('2025-05-08 09:30:00')),
    (GENERATE_UUID(), 'user_A', '2025-05-18', 'Tesco Express', 28.00, 'GBP', TIMESTAMP('2025-05-18 10:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-05-29', 'BP Garage', 58.00, 'GBP', TIMESTAMP('2025-05-29 11:30:00')),
    (GENERATE_UUID(), 'user_A', '2025-06-02', 'Gas Bill May', 40.00, 'GBP', TIMESTAMP('2025-06-02 09:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-06-15', 'Uber Ride', 19.00, 'GBP', TIMESTAMP('2025-06-15 14:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-06-25', 'Supermarket', 60.00, 'GBP', TIMESTAMP('2025-06-25 16:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-07-01', 'Shell Fuel', 63.00, 'GBP', TIMESTAMP('2025-07-01 10:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-07-10', 'Groceries', 72.00, 'GBP', TIMESTAMP('2025-07-10 11:00:00')),
    (GENERATE_UUID(), 'user_A', '2025-07-20', 'Electricity Bill Jun', 75.00, 'GBP', TIMESTAMP('2025-07-20 09:00:00')),

    -- User_B (EV, Hotel, Electronics, Water) - Feb-Jul
    (GENERATE_UUID(), 'user_B', '2025-02-08', 'EV Charge - Home', 12.00, 'GBP', TIMESTAMP('2025-02-08 10:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-02-18', 'Hotel Stay Manchester', 95.00, 'GBP', TIMESTAMP('2025-02-18 11:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-02-20', 'Currys PC World', 499.00, 'GBP', TIMESTAMP('2025-02-20 09:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-03-05', 'Public EV Charge', 18.00, 'GBP', TIMESTAMP('2025-03-05 14:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-03-15', 'British Airways Flight', 180.00, 'GBP', TIMESTAMP('2025-03-15 16:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-03-25', 'Water Services Quarterly', 60.00, 'GBP', TIMESTAMP('2025-03-25 10:30:00')),
    (GENERATE_UUID(), 'user_B', '2025-04-01', 'EV Charge - Home', 15.00, 'GBP', TIMESTAMP('2025-04-01 12:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-04-10', 'Travelodge Edinburgh', 85.00, 'GBP', TIMESTAMP('2025-04-10 13:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-04-22', 'Fashion Outlet', 120.00, 'GBP', TIMESTAMP('2025-04-22 15:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-05-03', 'Ionity Charger', 20.00, 'GBP', TIMESTAMP('2025-05-03 09:30:00')),
    (GENERATE_UUID(), 'user_B', '2025-05-13', 'Hilton Stay', 110.00, 'GBP', TIMESTAMP('2025-05-13 10:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-05-23', 'Apple Store', 900.00, 'GBP', TIMESTAMP('2025-05-23 11:30:00')),
    (GENERATE_UUID(), 'user_B', '2025-06-06', 'EV Charge - Home', 14.00, 'GBP', TIMESTAMP('2025-06-06 09:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-06-16', 'Thames Water Bill', 65.00, 'GBP', TIMESTAMP('2025-06-16 14:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-06-26', 'Currys', 250.00, 'GBP', TIMESTAMP('2025-06-26 16:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-07-05', 'Public EV Charge', 16.00, 'GBP', TIMESTAMP('2025-07-05 10:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-07-15', 'Hotel Brighton', 105.00, 'GBP', TIMESTAMP('2025-07-15 11:00:00')),
    (GENERATE_UUID(), 'user_B', '2025-07-25', 'Water Services', 58.00, 'GBP', TIMESTAMP('2025-07-25 09:00:00')),

    -- User_C (Public transport heavy, some shopping, waste) - Feb-Jul
    (GENERATE_UUID(), 'user_C', '2025-02-01', 'Bus Ticket Daily', 5.00, 'GBP', TIMESTAMP('2025-02-01 10:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-02-10', 'Amazon UK', 25.00, 'GBP', TIMESTAMP('2025-02-10 11:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-02-20', 'National Rail Travel', 30.00, 'GBP', TIMESTAMP('2025-02-20 09:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-03-03', 'Bus Pass Monthly', 70.00, 'GBP', TIMESTAMP('2025-03-03 14:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-03-12', 'Zara Clothing', 85.00, 'GBP', TIMESTAMP('2025-03-12 16:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-03-22', 'Trainline Commute', 18.00, 'GBP', TIMESTAMP('2025-03-22 10:30:00')),
    (GENERATE_UUID(), 'user_C', '2025-04-05', 'Local Cabs', 10.00, 'GBP', TIMESTAMP('2025-04-05 12:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-04-15', 'Primark Haul', 40.00, 'GBP', TIMESTAMP('2025-04-15 13:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-04-28', 'Bus Ticket', 3.00, 'GBP', TIMESTAMP('2025-04-28 15:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-05-01', 'Recycling Centre Fee', 10.00, 'GBP', TIMESTAMP('2025-05-01 09:30:00')),
    (GENERATE_UUID(), 'user_C', '2025-05-11', 'Train Ticket', 28.00, 'GBP', TIMESTAMP('2025-05-11 10:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-05-21', 'H&M Shop', 60.00, 'GBP', TIMESTAMP('2025-05-21 11:30:00')),
    (GENERATE_UUID(), 'user_C', '2025-06-04', 'Bus Pass', 70.00, 'GBP', TIMESTAMP('2025-06-04 09:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-06-14', 'Skip Hire Payment', 50.00, 'GBP', TIMESTAMP('2025-06-14 14:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-06-24', 'National Rail', 35.00, 'GBP', TIMESTAMP('2025-06-24 16:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-07-03', 'Local Cabs', 12.00, 'GBP', TIMESTAMP('2025-07-03 10:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-07-13', 'Zara', 75.00, 'GBP', TIMESTAMP('2025-07-13 11:00:00')),
    (GENERATE_UUID(), 'user_C', '2025-07-23', 'Bus Ticket', 4.00, 'GBP', TIMESTAMP('2025-07-23 09:00:00')),

    -- User_D (Mixed, including motorbike, diesel car, gas) - Feb-Jul
    (GENERATE_UUID(), 'user_D', '2025-02-07', 'Motorbike Fuel', 20.00, 'GBP', TIMESTAMP('2025-02-07 10:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-02-16', 'Restaurant Dinner', 50.00, 'GBP', TIMESTAMP('2025-02-16 11:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-02-25', 'Utility Co. Electric', 90.00, 'GBP', TIMESTAMP('2025-02-25 09:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-03-08', 'Motorbike Service', 150.00, 'GBP', TIMESTAMP('2025-03-08 14:00:00')), -- No direct carbon impact from service
    (GENERATE_UUID(), 'user_D', '2025-03-18', 'Esso Diesel Refuel', 45.00, 'GBP', TIMESTAMP('2025-03-18 16:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-03-29', 'Water Bill', 55.00, 'GBP', TIMESTAMP('2025-03-29 10:30:00')),
    (GENERATE_UUID(), 'user_D', '2025-04-06', 'Motorbike Fuel', 25.00, 'GBP', TIMESTAMP('2025-04-06 12:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-04-16', 'Sainsburys', 60.00, 'GBP', TIMESTAMP('2025-04-16 13:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-04-29', 'Utility Co. Gas', 65.00, 'GBP', TIMESTAMP('2025-04-29 15:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-05-09', 'Motorbike Fuel', 22.00, 'GBP', TIMESTAMP('2025-05-09 09:30:00')),
    (GENERATE_UUID(), 'user_D', '2025-05-19', 'Texaco Diesel', 48.00, 'GBP', TIMESTAMP('2025-05-19 10:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-05-30', 'Electricity Bill', 92.00, 'GBP', TIMESTAMP('2025-05-30 11:30:00')),
    (GENERATE_UUID(), 'user_D', '2025-06-07', 'Motorbike Fuel', 28.00, 'GBP', TIMESTAMP('2025-06-07 09:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-06-17', 'Esso Diesel', 52.00, 'GBP', TIMESTAMP('2025-06-17 14:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-06-28', 'Water Bill', 57.00, 'GBP', TIMESTAMP('2025-06-28 16:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-07-06', 'Motorbike Fuel', 23.00, 'GBP', TIMESTAMP('2025-07-06 10:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-07-16', 'Utility Co. Gas', 68.00, 'GBP', TIMESTAMP('2025-07-16 11:00:00')),
    (GENERATE_UUID(), 'user_D', '2025-07-29', 'Diesel Refuel', 47.00, 'GBP', TIMESTAMP('2025-07-29 09:00:00')),

    -- User_E (More air travel, hotels, some taxi) - Feb-Jul
    (GENERATE_UUID(), 'user_E', '2025-02-10', 'Airline Ticket Rome', 300.00, 'GBP', TIMESTAMP('2025-02-10 10:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-02-12', 'Hotel Barcelona', 250.00, 'GBP', TIMESTAMP('2025-02-12 11:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-02-22', 'Taxi Airport', 40.00, 'GBP', TIMESTAMP('2025-02-22 09:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-03-01', 'Flight Ticket Dublin', 150.00, 'GBP', TIMESTAMP('2025-03-01 14:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-03-03', 'Premier Inn Dublin', 110.00, 'GBP', TIMESTAMP('2025-03-03 16:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-03-15', 'Uber Ride City', 18.00, 'GBP', TIMESTAMP('2025-03-15 10:30:00')),
    (GENERATE_UUID(), 'user_E', '2025-04-08', 'Airline Tickets US', 800.00, 'GBP', TIMESTAMP('2025-04-08 12:00:00')), -- Long-haul
    (GENERATE_UUID(), 'user_E', '2025-04-10', 'Hotel New York', 350.00, 'GBP', TIMESTAMP('2025-04-10 13:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-04-20', 'Local Cabs', 12.00, 'GBP', TIMESTAMP('2025-04-20 15:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-05-02', 'EasyJet UK', 70.00, 'GBP', TIMESTAMP('2025-05-02 09:30:00')), -- Domestic
    (GENERATE_UUID(), 'user_E', '2025-05-04', 'Travelodge', 90.00, 'GBP', TIMESTAMP('2025-05-04 10:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-05-14', 'Taxi Ride', 10.00, 'GBP', TIMESTAMP('2025-05-14 11:30:00')),
    (GENERATE_UUID(), 'user_E', '2025-06-01', 'Emirates Flight Dubai', 600.00, 'GBP', TIMESTAMP('2025-06-01 09:00:00')), -- Long-haul
    (GENERATE_UUID(), 'user_E', '2025-06-03', 'Hotel Dubai', 280.00, 'GBP', TIMESTAMP('2025-06-03 14:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-06-13', 'Uber', 25.00, 'GBP', TIMESTAMP('2025-06-13 16:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-07-08', 'BA Domestic Flight', 110.00, 'GBP', TIMESTAMP('2025-07-08 10:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-07-10', 'Hotel Edinburgh', 130.00, 'GBP', TIMESTAMP('2025-07-10 11:00:00')),
    (GENERATE_UUID(), 'user_E', '2025-07-20', 'Taxi', 16.00, 'GBP', TIMESTAMP('2025-07-20 09:00:00')),

    -- User_F (Consistent everyday spending, water bill, some wood/coal) - Feb-Jul
    (GENERATE_UUID(), 'user_F', '2025-02-02', 'Lidl Groceries', 28.00, 'GBP', TIMESTAMP('2025-02-02 10:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-02-14', 'BP Garage', 55.00, 'GBP', TIMESTAMP('2025-02-14 11:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-02-26', 'Electricity Bill', 70.00, 'GBP', TIMESTAMP('2025-02-26 09:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-03-07', 'Co-op Food', 22.50, 'GBP', TIMESTAMP('2025-03-07 14:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-03-19', 'Petrol Station', 58.00, 'GBP', TIMESTAMP('2025-03-19 16:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-03-30', 'Gas Bill', 48.00, 'GBP', TIMESTAMP('2025-03-30 10:30:00')),
    (GENERATE_UUID(), 'user_F', '2025-04-04', 'Waitrose', 38.00, 'GBP', TIMESTAMP('2025-04-04 12:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-04-17', 'Shell', 62.00, 'GBP', TIMESTAMP('2025-04-17 13:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-04-27', 'Utility Co. Water', 40.00, 'GBP', TIMESTAMP('2025-04-27 15:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-05-05', 'Firewood Logs', 30.00, 'GBP', TIMESTAMP('2025-05-05 09:30:00')), -- Wood
    (GENERATE_UUID(), 'user_F', '2025-05-15', 'Groceries', 50.00, 'GBP', TIMESTAMP('2025-05-15 10:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-05-28', 'Electricity Bill', 72.00, 'GBP', TIMESTAMP('2025-05-28 11:30:00')),
    (GENERATE_UUID(), 'user_F', '2025-06-08', 'Coal Merchant', 40.00, 'GBP', TIMESTAMP('2025-06-08 09:00:00')), -- Coal
    (GENERATE_UUID(), 'user_F', '2025-06-18', 'Petrol Station', 59.00, 'GBP', TIMESTAMP('2025-06-18 14:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-06-29', 'Gas Bill', 45.00, 'GBP', TIMESTAMP('2025-06-29 16:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-07-07', 'Utility Co. Water', 42.00, 'GBP', TIMESTAMP('2025-07-07 10:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-07-17', 'Groceries', 55.00, 'GBP', TIMESTAMP('2025-07-17 11:00:00')),
    (GENERATE_UUID(), 'user_F', '2025-07-28', 'Shell', 60.00, 'GBP', TIMESTAMP('2025-07-28 09:00:00')),

    -- User_G (Focus on electronics/clothing, some taxi, gas) - Feb-Jul
    (GENERATE_UUID(), 'user_G', '2025-02-09', 'Tech Shop Gadget', 750.00, 'GBP', TIMESTAMP('2025-02-09 10:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-02-19', 'H&M Clothing', 90.00, 'GBP', TIMESTAMP('2025-02-19 11:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-02-23', 'Local Cabs', 16.00, 'GBP', TIMESTAMP('2025-02-23 09:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-03-11', 'Gaming PC Store', 1200.00, 'GBP', TIMESTAMP('2025-03-11 14:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-03-21', 'Fashion Boutique', 150.00, 'GBP', TIMESTAMP('2025-03-21 16:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-03-26', 'Uber', 20.00, 'GBP', TIMESTAMP('2025-03-26 10:30:00')),
    (GENERATE_UUID(), 'user_G', '2025-04-07', 'Currys', 300.00, 'GBP', TIMESTAMP('2025-04-07 12:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-04-18', 'Zara', 70.00, 'GBP', TIMESTAMP('2025-04-18 13:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-04-24', 'Taxi Ride', 14.00, 'GBP', TIMESTAMP('2025-04-24 15:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-05-06', 'Utility Co. Gas', 80.00, 'GBP', TIMESTAMP('2025-05-06 09:30:00')),
    (GENERATE_UUID(), 'user_G', '2025-05-16', 'Electronics Store', 400.00, 'GBP', TIMESTAMP('2025-05-16 10:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-05-26', 'H&M', 55.00, 'GBP', TIMESTAMP('2025-05-26 11:30:00')),
    (GENERATE_UUID(), 'user_G', '2025-06-09', 'Uber', 22.00, 'GBP', TIMESTAMP('2025-06-09 09:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-06-19', 'Fashion Nova', 110.00, 'GBP', TIMESTAMP('2025-06-19 14:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-06-29', 'Currys PC World', 600.00, 'GBP', TIMESTAMP('2025-06-29 16:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-07-09', 'Local Cabs', 18.00, 'GBP', TIMESTAMP('2025-07-09 10:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-07-19', 'Zara', 80.00, 'GBP', TIMESTAMP('2025-07-19 11:00:00')),
    (GENERATE_UUID(), 'user_G', '2025-07-29', 'Gas Bill', 78.00, 'GBP', TIMESTAMP('2025-07-29 09:00:00')),

    -- User_H (Consistent EV use, some minor expenses) - Feb-Jul
    (GENERATE_UUID(), 'user_H', '2025-02-04', 'EV Charge - Home', 10.00, 'GBP', TIMESTAMP('2025-02-04 10:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-02-14', 'Cafe Nero', 5.50, 'GBP', TIMESTAMP('2025-02-14 11:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-02-24', 'Public EV Charge', 8.00, 'GBP', TIMESTAMP('2025-02-24 09:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-03-06', 'EV Charge - Home', 11.00, 'GBP', TIMESTAMP('2025-03-06 14:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-03-16', 'Greggs', 4.00, 'GBP', TIMESTAMP('2025-03-16 16:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-03-27', 'Public EV Charger', 9.00, 'GBP', TIMESTAMP('2025-03-27 10:30:00')),
    (GENERATE_UUID(), 'user_H', '2025-04-02', 'EV Charge - Home', 13.00, 'GBP', TIMESTAMP('2025-04-02 12:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-04-13', 'Pret A Manger', 7.00, 'GBP', TIMESTAMP('2025-04-13 13:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-04-26', 'Public EV Chargepoint', 10.00, 'GBP', TIMESTAMP('2025-04-26 15:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-05-04', 'EV Charge - Home', 12.00, 'GBP', TIMESTAMP('2025-05-04 09:30:00')),
    (GENERATE_UUID(), 'user_H', '2025-05-14', 'Cafe', 6.00, 'GBP', TIMESTAMP('2025-05-14 10:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-05-24', 'Ionity Charger', 9.50, 'GBP', TIMESTAMP('2025-05-24 11:30:00')),
    (GENERATE_UUID(), 'user_H', '2025-06-06', 'EV Charge - Home', 11.50, 'GBP', TIMESTAMP('2025-06-06 09:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-06-16', 'Coffee Shop', 4.50, 'GBP', TIMESTAMP('2025-06-16 14:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-06-27', 'Public EV Charge', 8.50, 'GBP', TIMESTAMP('2025-06-27 16:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-07-02', 'EV Charge - Home', 14.00, 'GBP', TIMESTAMP('2025-07-02 10:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-07-13', 'Bakery', 5.00, 'GBP', TIMESTAMP('2025-07-13 11:00:00')),
    (GENERATE_UUID(), 'user_H', '2025-07-26', 'EV Charging', 11.00, 'GBP', TIMESTAMP('2025-07-26 09:00:00')),

    -- User_I (Mix of public transport and general shopping, some LPG) - Feb-Jul
    (GENERATE_UUID(), 'user_I', '2025-02-06', 'Transport for London Bus', 8.00, 'GBP', TIMESTAMP('2025-02-06 10:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-02-17', 'Sainsburys', 50.00, 'GBP', TIMESTAMP('2025-02-17 11:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-02-27', 'Train Ticket', 25.00, 'GBP', TIMESTAMP('2025-02-27 09:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-03-09', 'Tesco', 65.00, 'GBP', TIMESTAMP('2025-03-09 14:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-03-19', 'Bus Pass', 60.00, 'GBP', TIMESTAMP('2025-03-19 16:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-03-28', 'National Rail', 32.00, 'GBP', TIMESTAMP('2025-03-28 10:30:00')),
    (GENERATE_UUID(), 'user_I', '2025-04-05', 'Aldi', 48.00, 'GBP', TIMESTAMP('2025-04-05 12:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-04-14', 'London Underground', 10.00, 'GBP', TIMESTAMP('2025-04-14 13:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-04-21', 'Local Bus', 6.00, 'GBP', TIMESTAMP('2025-04-21 15:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-05-07', 'LPG Delivery', 40.00, 'GBP', TIMESTAMP('2025-05-07 09:30:00')), -- LPG
    (GENERATE_UUID(), 'user_I', '2025-05-17', 'Trainline', 27.00, 'GBP', TIMESTAMP('2025-05-17 10:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-05-27', 'Sainsburys', 55.00, 'GBP', TIMESTAMP('2025-05-27 11:30:00')),
    (GENERATE_UUID(), 'user_I', '2025-06-05', 'Bus Ticket', 7.00, 'GBP', TIMESTAMP('2025-06-05 09:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-06-15', 'Tesco', 62.00, 'GBP', TIMESTAMP('2025-06-15 14:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-06-25', 'National Rail', 30.00, 'GBP', TIMESTAMP('2025-06-25 16:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-07-04', 'LPG Bottle', 35.00, 'GBP', TIMESTAMP('2025-07-04 10:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-07-14', 'Aldi', 50.00, 'GBP', TIMESTAMP('2025-07-14 11:00:00')),
    (GENERATE_UUID(), 'user_I', '2025-07-24', 'London Bus', 9.00, 'GBP', TIMESTAMP('2025-07-24 09:00:00')),

    -- User_J (More varied spending, some larger purchases, air travel) - Feb-Jul
    (GENERATE_UUID(), 'user_J', '2025-02-11', 'Petrol Station', 70.00, 'GBP', TIMESTAMP('2025-02-11 10:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-02-21', 'Online Fashion Store', 180.00, 'GBP', TIMESTAMP('2025-02-21 11:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-02-28', 'Electricity Bill March', 95.00, 'GBP', TIMESTAMP('2025-02-28 09:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-03-04', 'Hotel Getaway', 150.00, 'GBP', TIMESTAMP('2025-03-04 14:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-03-14', 'Tech Gadget', 800.00, 'GBP', TIMESTAMP('2025-03-14 16:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-03-24', 'Diesel Refuel', 50.00, 'GBP', TIMESTAMP('2025-03-24 10:30:00')),
    (GENERATE_UUID(), 'user_J', '2025-04-09', 'Water Utilities', 70.00, 'GBP', TIMESTAMP('2025-04-09 12:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-04-19', 'Flight to Edinburgh', 90.00, 'GBP', TIMESTAMP('2025-04-19 13:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-04-30', 'Supermarket Large Shop', 105.00, 'GBP', TIMESTAMP('2025-04-30 15:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-05-10', 'Petrol', 68.00, 'GBP', TIMESTAMP('2025-05-10 09:30:00')),
    (GENERATE_UUID(), 'user_J', '2025-05-20', 'Clothing Store', 100.00, 'GBP', TIMESTAMP('2025-05-20 10:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-05-31', 'Electricity Bill Apr', 90.00, 'GBP', TIMESTAMP('2025-05-31 11:30:00')),
    (GENERATE_UUID(), 'user_J', '2025-06-02', 'Hotel London', 130.00, 'GBP', TIMESTAMP('2025-06-02 09:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-06-12', 'Electronics', 500.00, 'GBP', TIMESTAMP('2025-06-12 14:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-06-22', 'Diesel', 55.00, 'GBP', TIMESTAMP('2025-06-22 16:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-07-07', 'Water Utilities', 72.00, 'GBP', TIMESTAMP('2025-07-07 10:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-07-17', 'Flight to Manchester', 80.00, 'GBP', TIMESTAMP('2025-07-17 11:00:00')),
    (GENERATE_UUID(), 'user_J', '2025-07-27', 'Supermarket Large Shop', 98.00, 'GBP', TIMESTAMP('2025-07-27 09:00:00')),

    -- User_K (Sea Travel, more varied utilities) - Feb-Jul
    (GENERATE_UUID(), 'user_K', '2025-02-03', 'Ferry Ticket Dover', 25.00, 'GBP', TIMESTAMP('2025-02-03 10:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-02-13', 'Coal Merchant Purchase', 30.00, 'GBP', TIMESTAMP('2025-02-13 11:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-02-23', 'Electricity Provider', 60.00, 'GBP', TIMESTAMP('2025-02-23 09:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-03-06', 'Ferry to Isle of Wight', 15.00, 'GBP', TIMESTAMP('2025-03-06 14:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-03-16', 'Wood Logs Delivered', 20.00, 'GBP', TIMESTAMP('2025-03-16 16:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-03-26', 'Gas Bill Payment', 40.00, 'GBP', TIMESTAMP('2025-03-26 10:30:00')),
    (GENERATE_UUID(), 'user_K', '2025-04-01', 'DFDS Ferry', 28.00, 'GBP', TIMESTAMP('2025-04-01 12:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-04-11', 'Coal Supply', 35.00, 'GBP', TIMESTAMP('2025-04-11 13:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-04-21', 'Electricity Bill', 65.00, 'GBP', TIMESTAMP('2025-04-21 15:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-05-03', 'Ferry Crossing', 20.00, 'GBP', TIMESTAMP('2025-05-03 09:30:00')),
    (GENERATE_UUID(), 'user_K', '2025-05-13', 'Wood Fuel', 25.00, 'GBP', TIMESTAMP('2025-05-13 10:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-05-23', 'Gas Bill', 42.00, 'GBP', TIMESTAMP('2025-05-23 11:30:00')),
    (GENERATE_UUID(), 'user_K', '2025-06-05', 'Ferry Ticket', 26.00, 'GBP', TIMESTAMP('2025-06-05 09:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-06-15', 'Coal', 32.00, 'GBP', TIMESTAMP('2025-06-15 14:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-06-25', 'Electricity', 68.00, 'GBP', TIMESTAMP('2025-06-25 16:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-07-03', 'Ferry', 24.00, 'GBP', TIMESTAMP('2025-07-03 10:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-07-13', 'Wood Logs', 22.00, 'GBP', TIMESTAMP('2025-07-13 11:00:00')),
    (GENERATE_UUID(), 'user_K', '2025-07-23', 'Gas', 44.00, 'GBP', TIMESTAMP('2025-07-23 09:00:00')),

    -- User_L (Long-haul travel, electronics, some waste) - Feb-Jul
    (GENERATE_UUID(), 'user_L', '2025-02-14', 'Emirates Flight Dubai', 700.00, 'GBP', TIMESTAMP('2025-02-14 10:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-02-24', 'Gaming PC Store', 1500.00, 'GBP', TIMESTAMP('2025-02-24 11:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-03-01', 'Hotel Stay Dubai', 300.00, 'GBP', TIMESTAMP('2025-03-01 09:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-03-11', 'Waste Removal Service', 80.00, 'GBP', TIMESTAMP('2025-03-11 14:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-03-21', 'British Airways Flight NYC', 950.00, 'GBP', TIMESTAMP('2025-03-21 16:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-04-02', 'Hotel New York', 400.00, 'GBP', TIMESTAMP('2025-04-02 10:30:00')),
    (GENERATE_UUID(), 'user_L', '2025-04-12', 'Currys PC World', 600.00, 'GBP', TIMESTAMP('2025-04-12 12:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-04-22', 'Skip Hire', 70.00, 'GBP', TIMESTAMP('2025-04-22 13:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-05-05', 'Virgin Atlantic Flight', 850.00, 'GBP', TIMESTAMP('2025-05-05 15:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-05-15', 'Tech Gadget Shop', 250.00, 'GBP', TIMESTAMP('2025-05-15 09:30:00')),
    (GENERATE_UUID(), 'user_L', '2025-05-25', 'Hotel Singapore', 320.00, 'GBP', TIMESTAMP('2025-05-25 10:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-06-07', 'Waste Disposal', 90.00, 'GBP', TIMESTAMP('2025-06-07 11:30:00')),
    (GENERATE_UUID(), 'user_L', '2025-06-17', 'Flight to Tokyo', 1100.00, 'GBP', TIMESTAMP('2025-06-17 09:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-06-27', 'Electronics Retailer', 700.00, 'GBP', TIMESTAMP('2025-06-27 14:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-07-09', 'Hotel Tokyo', 450.00, 'GBP', TIMESTAMP('2025-07-09 16:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-07-19', 'Skip Hire', 75.00, 'GBP', TIMESTAMP('2025-07-19 10:00:00')),
    (GENERATE_UUID(), 'user_L', '2025-07-29', 'Long-haul Flight', 980.00, 'GBP', TIMESTAMP('2025-07-29 11:00:00')),

    -- User_M (Diesel, Clothing, Groceries) - Feb-Jul
    (GENERATE_UUID(), 'user_M', '2025-02-06', 'Texaco Diesel', 50.00, 'GBP', TIMESTAMP('2025-02-06 10:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-02-16', 'Next Shop Clothing', 75.00, 'GBP', TIMESTAMP('2025-02-16 11:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-02-26', 'Morrisons Groceries', 40.00, 'GBP', TIMESTAMP('2025-02-26 09:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-03-08', 'Esso Diesel', 48.00, 'GBP', TIMESTAMP('2025-03-08 14:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-03-18', 'Zara Online', 95.00, 'GBP', TIMESTAMP('2025-03-18 16:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-03-28', 'Tesco', 60.00, 'GBP', TIMESTAMP('2025-03-28 10:30:00')),
    (GENERATE_UUID(), 'user_M', '2025-04-04', 'Diesel Station', 52.00, 'GBP', TIMESTAMP('2025-04-04 12:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-04-14', 'Clothing Retailer', 80.00, 'GBP', TIMESTAMP('2025-04-14 13:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-04-24', 'Sainsburys', 55.00, 'GBP', TIMESTAMP('2025-04-24 15:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-05-06', 'Diesel Refuel', 53.00, 'GBP', TIMESTAMP('2025-05-06 09:30:00')),
    (GENERATE_UUID(), 'user_M', '2025-05-16', 'Fashion Store', 70.00, 'GBP', TIMESTAMP('2025-05-16 10:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-05-26', 'Aldi', 45.00, 'GBP', TIMESTAMP('2025-05-26 11:30:00')),
    (GENERATE_UUID(), 'user_M', '2025-06-08', 'Diesel', 50.00, 'GBP', TIMESTAMP('2025-06-08 09:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-06-18', 'Next', 85.00, 'GBP', TIMESTAMP('2025-06-18 14:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-06-28', 'Morrisons', 48.00, 'GBP', TIMESTAMP('2025-06-28 16:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-07-06', 'Diesel', 49.00, 'GBP', TIMESTAMP('2025-07-06 10:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-07-16', 'Zara', 90.00, 'GBP', TIMESTAMP('2025-07-16 11:00:00')),
    (GENERATE_UUID(), 'user_M', '2025-07-26', 'Tesco', 62.00, 'GBP', TIMESTAMP('2025-07-26 09:00:00')),

    -- User_N (Gas, Taxi, General Consumption) - Feb-Jul
    (GENERATE_UUID(), 'user_N', '2025-02-09', 'Gas Bill February', 70.00, 'GBP', TIMESTAMP('2025-02-09 10:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-02-19', 'Uber Ride', 12.00, 'GBP', TIMESTAMP('2025-02-19 11:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-02-28', 'Local Restaurant', 30.00, 'GBP', TIMESTAMP('2025-02-28 09:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-03-05', 'Utility Co. Gas', 65.00, 'GBP', TIMESTAMP('2025-03-05 14:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-03-15', 'Taxi', 10.00, 'GBP', TIMESTAMP('2025-03-15 16:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-03-25', 'Book Store', 20.00, 'GBP', TIMESTAMP('2025-03-25 10:30:00')),
    (GENERATE_UUID(), 'user_N', '2025-04-07', 'Gas Bill', 72.00, 'GBP', TIMESTAMP('2025-04-07 12:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-04-17', 'Bolt Ride', 14.00, 'GBP', TIMESTAMP('2025-04-17 13:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-04-27', 'Coffee Shop', 8.00, 'GBP', TIMESTAMP('2025-04-27 15:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-05-09', 'Gas Bill', 68.00, 'GBP', TIMESTAMP('2025-05-09 09:30:00')),
    (GENERATE_UUID(), 'user_N', '2025-05-19', 'Taxi Fare', 11.00, 'GBP', TIMESTAMP('2025-05-19 10:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-05-29', 'Cinema Tickets', 25.00, 'GBP', TIMESTAMP('2025-05-29 11:30:00')),
    (GENERATE_UUID(), 'user_N', '2025-06-05', 'Gas Bill', 70.00, 'GBP', TIMESTAMP('2025-06-05 09:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-06-15', 'Uber', 13.00, 'GBP', TIMESTAMP('2025-06-15 14:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-06-25', 'Restaurant', 35.00, 'GBP', TIMESTAMP('2025-06-25 16:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-07-08', 'Gas Bill', 67.00, 'GBP', TIMESTAMP('2025-07-08 10:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-07-18', 'Taxi', 10.00, 'GBP', TIMESTAMP('2025-07-18 11:00:00')),
    (GENERATE_UUID(), 'user_N', '2025-07-28', 'Online Purchase', 15.00, 'GBP', TIMESTAMP('2025-07-28 09:00:00')),

    -- User_O (Mixed public transport, general shopping, water) - Feb-Jul
    (GENERATE_UUID(), 'user_O', '2025-02-01', 'Bus Ticket', 6.00, 'GBP', TIMESTAMP('2025-02-01 10:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-02-11', 'Waitrose Groceries', 45.00, 'GBP', TIMESTAMP('2025-02-11 11:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-02-21', 'Train Ticket', 20.00, 'GBP', TIMESTAMP('2025-02-21 09:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-03-03', 'Transport for London', 8.00, 'GBP', TIMESTAMP('2025-03-03 14:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-03-13', 'Sainsburys Local', 38.00, 'GBP', TIMESTAMP('2025-03-13 16:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-03-23', 'National Rail', 28.00, 'GBP', TIMESTAMP('2025-03-23 10:30:00')),
    (GENERATE_UUID(), 'user_O', '2025-04-05', 'Bus Pass', 65.00, 'GBP', TIMESTAMP('2025-04-05 12:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-04-15', 'Tesco Express', 25.00, 'GBP', TIMESTAMP('2025-04-15 13:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-04-25', 'Water Services', 50.00, 'GBP', TIMESTAMP('2025-04-25 15:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-05-02', 'Trainline Ticket', 22.00, 'GBP', TIMESTAMP('2025-05-02 09:30:00')),
    (GENERATE_UUID(), 'user_O', '2025-05-12', 'Aldi', 40.00, 'GBP', TIMESTAMP('2025-05-12 10:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-05-22', 'Bus Ticket', 5.00, 'GBP', TIMESTAMP('2025-05-22 11:30:00')),
    (GENERATE_UUID(), 'user_O', '2025-06-04', 'National Rail Travel', 30.00, 'GBP', TIMESTAMP('2025-06-04 09:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-06-14', 'Groceries', 55.00, 'GBP', TIMESTAMP('2025-06-14 14:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-06-24', 'Water Bill', 52.00, 'GBP', TIMESTAMP('2025-06-24 16:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-07-02', 'Bus', 7.00, 'GBP', TIMESTAMP('2025-07-02 10:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-07-12', 'Sainsburys', 48.00, 'GBP', TIMESTAMP('2025-07-12 11:00:00')),
    (GENERATE_UUID(), 'user_O', '2025-07-22', 'Train Ticket', 26.00, 'GBP', TIMESTAMP('2025-07-22 09:00:00')),

    -- Additional users (P, Q, R, S, T) for a total of 15 users, 6 months
    -- User_P (More air travel, domestic focus)
    (GENERATE_UUID(), 'user_P', '2025-02-10', 'Flight Ticket Domestic', 120.00, 'GBP', TIMESTAMP('2025-02-10 10:00:00')),
    (GENERATE_UUID(), 'user_P', '2025-03-01', 'BA Domestic', 90.00, 'GBP', TIMESTAMP('2025-03-01 14:00:00')),
    (GENERATE_UUID(), 'user_P', '2025-04-08', 'EasyJet UK', 150.00, 'GBP', TIMESTAMP('2025-04-08 12:00:00')),
    (GENERATE_UUID(), 'user_P', '2025-05-02', 'Domestic Flight', 110.00, 'GBP', TIMESTAMP('2025-05-02 09:30:00')),
    (GENERATE_UUID(), 'user_P', '2025-06-01', 'Flight UK', 130.00, 'GBP', TIMESTAMP('2025-06-01 09:00:00')),
    (GENERATE_UUID(), 'user_P', '2025-07-08', 'Regional Flight', 100.00, 'GBP', TIMESTAMP('2025-07-08 10:00:00')),

    -- User_Q (LPG and Wood focused)
    (GENERATE_UUID(), 'user_Q', '2025-02-15', 'LPG Delivery', 60.00, 'GBP', TIMESTAMP('2025-02-15 11:00:00')),
    (GENERATE_UUID(), 'user_Q', '2025-03-05', 'Wood Logs Delivered', 40.00, 'GBP', TIMESTAMP('2025-03-05 14:00:00')),
    (GENERATE_UUID(), 'user_Q', '2025-04-03', 'Gas Bottle Refill', 55.00, 'GBP', TIMESTAMP('2025-04-03 12:00:00')),
    (GENERATE_UUID(), 'user_Q', '2025-05-08', 'Firewood', 35.00, 'GBP', TIMESTAMP('2025-05-08 09:30:00')),
    (GENERATE_UUID(), 'user_Q', '2025-06-02', 'LPG Supply', 62.00, 'GBP', TIMESTAMP('2025-06-02 09:00:00')),
    (GENERATE_UUID(), 'user_Q', '2025-07-01', 'Wood Fuel Order', 45.00, 'GBP', TIMESTAMP('2025-07-01 10:00:00')),

    -- User_R (Diesel Car, some electronics)
    (GENERATE_UUID(), 'user_R', '2025-02-05', 'Esso Diesel', 70.00, 'GBP', TIMESTAMP('2025-02-05 10:00:00')),
    (GENERATE_UUID(), 'user_R', '2025-03-10', 'Texaco Diesel', 68.00, 'GBP', TIMESTAMP('2025-03-10 14:00:00')),
    (GENERATE_UUID(), 'user_R', '2025-04-03', 'Currys Tech', 300.00, 'GBP', TIMESTAMP('2025-04-03 12:00:00')),
    (GENERATE_UUID(), 'user_R', '2025-05-08', 'Diesel Station', 72.00, 'GBP', TIMESTAMP('2025-05-08 09:30:00')),
    (GENERATE_UUID(), 'user_R', '2025-06-02', 'PC World', 400.00, 'GBP', TIMESTAMP('2025-06-02 09:00:00')),
    (GENERATE_UUID(), 'user_R', '2025-07-01', 'Diesel Refuel', 65.00, 'GBP', TIMESTAMP('2025-07-01 10:00:00')),

    -- User_S (Commuter, Bus and Rail)
    (GENERATE_UUID(), 'user_S', '2025-02-01', 'Bus Ticket', 8.00, 'GBP', TIMESTAMP('2025-02-01 10:00:00')),
    (GENERATE_UUID(), 'user_S', '2025-03-03', 'National Rail Commute', 40.00, 'GBP', TIMESTAMP('2025-03-03 14:00:00')),
    (GENERATE_UUID(), 'user_S', '2025-04-05', 'Transport for London', 12.00, 'GBP', TIMESTAMP('2025-04-05 12:00:00')),
    (GENERATE_UUID(), 'user_S', '2025-05-02', 'Trainline', 35.00, 'GBP', TIMESTAMP('2025-05-02 09:30:00')),
    (GENERATE_UUID(), 'user_S', '2025-06-04', 'Bus Pass', 60.00, 'GBP', TIMESTAMP('2025-06-04 09:00:00')),
    (GENERATE_UUID(), 'user_S', '2025-07-02', 'Rail Ticket', 38.00, 'GBP', TIMESTAMP('2025-07-02 10:00:00')),

    -- User_T (Mixed Shopping, some Taxi)
    (GENERATE_UUID(), 'user_T', '2025-02-15', 'Sainsburys', 55.00, 'GBP', TIMESTAMP('2025-02-15 11:00:00')),
    (GENERATE_UUID(), 'user_T', '2025-03-05', 'Uber Ride', 15.00, 'GBP', TIMESTAMP('2025-03-05 14:00:00')),
    (GENERATE_UUID(), 'user_T', '2025-04-03', 'Tesco', 60.00, 'GBP', TIMESTAMP('2025-04-03 12:00:00')),
    (GENERATE_UUID(), 'user_T', '2025-05-08', 'Local Cabs', 12.00, 'GBP', TIMESTAMP('2025-05-08 09:30:00')),
    (GENERATE_UUID(), 'user_T', '2025-06-02', 'Morrisons', 48.00, 'GBP', TIMESTAMP('2025-06-02 09:00:00')),
    (GENERATE_UUID(), 'user_T', '2025-07-01', 'Zara', 70.00, 'GBP', TIMESTAMP('2025-07-01 10:00:00'));

-- Insert calculated carbon footprint data into the final_carbon_footprint table
INSERT INTO `iconic-iridium-463506-v6.cloudcompass_scd2_composer.final_carbon_footprint` (
    carbon_footprint_id,
    activity_date,
    user_id,
    transaction_id,
    transaction_description,
    transaction_amount,
    activity_category,
    activity_subcategory,
    activity_type,
    activity_unit,
    activity_quantity,
    emission_factor_kg_co2e_per_unit,
    emission_scope,
    calculated_kg_co2e,
    data_source,
    notes,
    processed_timestamp
)
SELECT
    GENERATE_UUID() AS carbon_footprint_id,
    t.transaction_date AS activity_date,
    t.user_id,
    t.transaction_id,
    t.transaction_description,
    t.transaction_amount,
    m.mapped_activity_category AS activity_category,
    m.mapped_activity_subcategory AS activity_subcategory,
    m.mapped_activity_type AS activity_type,
    m.final_activity_unit AS activity_unit,
    -- Calculate activity_quantity based on derivation method
    CASE m.quantity_derivation_method
        WHEN 'MonetaryConversion' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, c.rate_per_unit),0)
        WHEN 'FixedQuantity' THEN coalesce(m.fixed_quantity_value,0)
        WHEN 'EstimateFromAmount' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, m.estimated_cost_per_activity_unit),0)
        ELSE 0
    END AS activity_quantity,
    ef.kg_co2e_per_unit AS emission_factor_kg_co2e_per_unit,
    ef.emission_scope,
    -- Calculate calculated_kg_co2e
    (
        CASE m.quantity_derivation_method
            WHEN 'MonetaryConversion' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, c.rate_per_unit),0)
            WHEN 'FixedQuantity' THEN coalesce(m.fixed_quantity_value,0)
            WHEN 'EstimateFromAmount' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, m.estimated_cost_per_activity_unit),0)
            ELSE 0
        END
    ) * ef.kg_co2e_per_unit AS calculated_kg_co2e,
    'UK Gov GHG Factors 2025' AS data_source,
    m.notes,
    CURRENT_TIMESTAMP() AS processed_timestamp
FROM
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transactions` AS t
JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transaction_activity_mappings` AS m
    ON REGEXP_CONTAINS(t.transaction_description, m.transaction_keyword_pattern)
    -- Join with the correct mapping version based on transaction date
    AND t.transaction_date BETWEEN m.effective_from_date AND m.effective_to_date
LEFT JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.conversions` AS c
    ON m.quantity_derivation_method = 'MonetaryConversion'
    AND m.conversion_table_category_key = c.category
    AND t.currency = c.currency
    AND EXTRACT(YEAR FROM t.transaction_date) = c.year
    -- Join with the correct conversion rate version based on transaction date
    AND t.transaction_date BETWEEN c.effective_from_date AND c.effective_to_date
LEFT JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.emission_factors` AS ef
    ON m.mapped_activity_category = ef.activity_category
    AND m.mapped_activity_subcategory = ef.activity_subcategory
    AND m.mapped_activity_type = ef.activity_type
    AND m.final_activity_unit = ef.unit
    AND EXTRACT(YEAR FROM t.transaction_date) = ef.year
    -- Join with the correct emission factor version based on transaction date
    AND t.transaction_date BETWEEN ef.effective_from_date AND ef.effective_to_date
WHERE
    ef.emission_factor_id IS NOT NULL
    AND NOT (m.multi_scope_output_flag AND REGEXP_CONTAINS(m.transaction_keyword_pattern, 'EV Charge - Home|Public EV Charge|Water Bill|Water Services')); -- Exclude these to be handled by specific multi-scope inserts


-- Separate INSERT for Scope 3 T&D for EV (example of handling multi-scope with separate statements)
INSERT INTO `iconic-iridium-463506-v6.cloudcompass_scd2_composer.final_carbon_footprint` (
    carbon_footprint_id,
    activity_date,
    user_id,
    transaction_id,
    transaction_description,
    transaction_amount,
    activity_category,
    activity_subcategory,
    activity_type,
    activity_unit,
    activity_quantity,
    emission_factor_kg_co2e_per_unit,
    emission_scope,
    calculated_kg_co2e,
    data_source,
    notes,
    processed_timestamp
)
SELECT
    GENERATE_UUID() AS carbon_footprint_id,
    t.transaction_date AS activity_date,
    t.user_id,
    t.transaction_id,
    t.transaction_description,
    t.transaction_amount,
    'Transportation' AS activity_category,
    'EV T&D Losses' AS activity_subcategory,
    'Supermini (BEV)' AS activity_type,
    'km' AS activity_unit,
    CASE m.quantity_derivation_method
        WHEN 'MonetaryConversion' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, c.rate_per_unit),0)
        WHEN 'FixedQuantity' THEN coalesce(m.fixed_quantity_value,0)
        WHEN 'EstimateFromAmount' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, m.estimated_cost_per_activity_unit),0)
        ELSE 0
    END AS activity_quantity,
    ef_tnd.kg_co2e_per_unit AS emission_factor_kg_co2e_per_unit,
    ef_tnd.emission_scope,
    (
        CASE m.quantity_derivation_method
            WHEN 'MonetaryConversion' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, c.rate_per_unit),0)
            WHEN 'FixedQuantity' THEN coalesce(m.fixed_quantity_value,0)
            WHEN 'EstimateFromAmount' THEN coalesce(SAFE_DIVIDE(t.transaction_amount, m.estimated_cost_per_activity_unit),0)
            ELSE NULL
        END
    ) * ef_tnd.kg_co2e_per_unit AS calculated_kg_co2e,
    'UK Gov GHG Factors 2025' AS data_source,
    'Derived from primary EV charge; represents T&D losses' AS notes,
    CURRENT_TIMESTAMP() AS processed_timestamp
FROM
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transactions` AS t
JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transaction_activity_mappings` AS m
    ON REGEXP_CONTAINS(t.transaction_description, m.transaction_keyword_pattern)
    AND t.transaction_date BETWEEN m.effective_from_date AND m.effective_to_date
    AND m.multi_scope_output_flag = TRUE
    AND REGEXP_CONTAINS(m.transaction_keyword_pattern, 'EV Charge - Home|Public EV Charge')
LEFT JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.conversions` AS c
    ON m.quantity_derivation_method = 'MonetaryConversion'
    AND m.conversion_table_category_key = c.category
    AND t.currency = c.currency
    AND EXTRACT(YEAR FROM t.transaction_date) = c.year
    AND t.transaction_date BETWEEN c.effective_from_date AND c.effective_to_date
LEFT JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.emission_factors` AS ef_tnd
    ON ef_tnd.activity_category = 'Transportation'
    AND ef_tnd.activity_subcategory = 'EV T&D Losses'
    AND ef_tnd.activity_type = 'Supermini (BEV)'
    AND ef_tnd.unit = 'km'
    AND EXTRACT(YEAR FROM t.transaction_date) = ef_tnd.year
    AND t.transaction_date BETWEEN ef_tnd.effective_from_date AND ef_tnd.effective_to_date
WHERE
    ef_tnd.emission_factor_id IS NOT NULL;

-- Separate INSERT for Water Treatment for Water Supply (another example of handling multi-scope)
INSERT INTO `iconic-iridium-463506-v6.cloudcompass_scd2_composer.final_carbon_footprint` (
    carbon_footprint_id,
    activity_date,
    user_id,
    transaction_id,
    transaction_description,
    transaction_amount,
    activity_category,
    activity_subcategory,
    activity_type,
    activity_unit,
    activity_quantity,
    emission_factor_kg_co2e_per_unit,
    emission_scope,
    calculated_kg_co2e,
    data_source,
    notes,
    processed_timestamp
)
SELECT
    GENERATE_UUID() AS carbon_footprint_id,
    t.transaction_date AS activity_date,
    t.user_id,
    t.transaction_id,
    t.transaction_description,
    t.transaction_amount,
    'Utilities' AS activity_category,
    'Water treatment' AS activity_subcategory,
    'Average' AS activity_type,
    'm3' AS activity_unit,
    SAFE_DIVIDE(t.transaction_amount, c.rate_per_unit) AS activity_quantity,
    ef_wt.kg_co2e_per_unit AS emission_factor_kg_co2e_per_unit,
    ef_wt.emission_scope,
    SAFE_DIVIDE(t.transaction_amount, c.rate_per_unit) * ef_wt.kg_co2e_per_unit AS calculated_kg_co2e,
    'UK Gov GHG Factors 2025' AS data_source,
    'Derived from water bill; represents water treatment emissions' AS notes,
    CURRENT_TIMESTAMP() AS processed_timestamp
FROM
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transactions` AS t
JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.transaction_activity_mappings` AS m
    ON REGEXP_CONTAINS(t.transaction_description, m.transaction_keyword_pattern)
    AND t.transaction_date BETWEEN m.effective_from_date AND m.effective_to_date
    AND m.multi_scope_output_flag = TRUE
    AND REGEXP_CONTAINS(m.transaction_keyword_pattern, 'Water Bill|Water Services')
LEFT JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.conversions` AS c
    ON m.quantity_derivation_method = 'MonetaryConversion'
    AND m.conversion_table_category_key = c.category
    AND t.currency = c.currency
    AND EXTRACT(YEAR FROM t.transaction_date) = c.year
    AND t.transaction_date BETWEEN c.effective_from_date AND c.effective_to_date
LEFT JOIN
    `iconic-iridium-463506-v6.cloudcompass_scd2_composer.emission_factors` AS ef_wt
    ON ef_wt.activity_category = 'Utilities'
    AND ef_wt.activity_subcategory = 'Water treatment'
    AND ef_wt.activity_type = 'Average'
    AND ef_wt.unit = 'm3'
    AND EXTRACT(YEAR FROM t.transaction_date) = ef_wt.year
    AND t.transaction_date BETWEEN ef_wt.effective_from_date AND ef_wt.effective_to_date
WHERE
    ef_wt.emission_factor_id IS NOT NULL;

