-- Claimant-reported identifiers, used by R06.
ALTER TABLE claims
    ADD COLUMN reported_registration_number VARCHAR(20),
    ADD COLUMN reported_vin VARCHAR(50);

-- Structured repairer identifiers, used by R05.
ALTER TABLE repair_invoices
    ADD COLUMN repairer_gstin VARCHAR(15),
    ADD COLUMN repairer_phone VARCHAR(20);

-- Permit the new entity-match type.
ALTER TABLE entity_matches
    DROP CONSTRAINT IF EXISTS chk_entity_match_type;

ALTER TABLE entity_matches
    ADD CONSTRAINT chk_entity_match_type
    CHECK (
        match_type IN (
            'DUPLICATE_CLAIM',
            'SAME_VEHICLE',
            'SAME_CUSTOMER',
            'REPEATED_INVOICE',
            'SAME_REPAIRER',
            'INCONSISTENT_IDENTIFIER',
            'OTHER'
        )
    );

CREATE INDEX idx_repair_invoice_number
    ON repair_invoices (LOWER(invoice_number));

CREATE INDEX idx_repair_invoice_gstin
    ON repair_invoices (UPPER(repairer_gstin));

CREATE INDEX idx_repair_invoice_phone
    ON repair_invoices (repairer_phone);

CREATE INDEX idx_claims_reported_registration
    ON claims (UPPER(reported_registration_number));

CREATE INDEX idx_claims_reported_vin
    ON claims (UPPER(reported_vin));