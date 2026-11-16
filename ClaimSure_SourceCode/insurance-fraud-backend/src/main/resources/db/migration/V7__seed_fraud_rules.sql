-- ==========================================
-- V7: SEED INITIAL FRAUD DETECTION RULES
-- ==========================================

INSERT INTO fraud_rule_configurations (
    rule_code,
    rule_name,
    description,
    weight,
    enabled
)
VALUES

(
    'R01',
    'Exact Duplicate Claim',
    'Detects claims that appear to be exact duplicates of previously submitted claims.',
    40,
    TRUE
),

(
    'R02',
    'Same Vehicle and Incident Date',
    'Detects multiple claims associated with the same vehicle and incident date.',
    25,
    TRUE
),

(
    'R03',
    'Multiple Recent Claims by Customer',
    'Detects unusually frequent claims submitted by the same customer within a defined time period.',
    20,
    TRUE
),

(
    'R04',
    'Repeated Repair Invoices',
    'Detects the reuse of the same repair invoice across multiple claims.',
    15,
    TRUE
),

(
    'R05',
    'Suspicious Entity Cluster',
    'Detects potentially suspicious relationships among claims, vehicles, customers and repairers.',
    20,
    TRUE
),

(
    'R06',
    'Inconsistent Identifiers',
    'Detects inconsistencies in claim, vehicle, policy or invoice identifiers.',
    15,
    TRUE
);
