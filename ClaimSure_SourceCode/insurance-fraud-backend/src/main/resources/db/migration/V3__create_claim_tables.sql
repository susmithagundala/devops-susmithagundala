-- =====================================================
-- V3: Create Claims, Evidence and Repair Invoice Tables
-- =====================================================


-- 1. CLAIMS
-- Stores insurance claim submissions.

CREATE TABLE claims (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    claim_number VARCHAR(50) NOT NULL UNIQUE,

    customer_id BIGINT NOT NULL,

    policy_id BIGINT NOT NULL,

    vehicle_id BIGINT NOT NULL,

    submitted_by_user_id BIGINT NOT NULL,

    incident_date DATE NOT NULL,

    incident_location VARCHAR(255),

    incident_description TEXT NOT NULL,

    claimed_amount NUMERIC(14,2) NOT NULL,

    risk_score INTEGER NOT NULL DEFAULT 0,

    risk_band VARCHAR(20) NOT NULL DEFAULT 'LOW',

    status VARCHAR(40) NOT NULL DEFAULT 'SUBMITTED',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_claims_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_claims_policy_vehicle
        FOREIGN KEY (policy_id, vehicle_id)
        REFERENCES policy_vehicles(policy_id, vehicle_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_claims_submitted_by
        FOREIGN KEY (submitted_by_user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_claimed_amount
        CHECK (claimed_amount >= 0),

    CONSTRAINT chk_claim_risk_score
        CHECK (risk_score >= 0),

    CONSTRAINT chk_claim_risk_band
        CHECK (risk_band IN ('LOW', 'MEDIUM', 'HIGH')),

    CONSTRAINT chk_claim_status
        CHECK (
            status IN (
                'SUBMITTED',
                'UNDER_REVIEW',
                'ADDITIONAL_EVIDENCE_REQUESTED',
                'CLEARED_FOR_PROCESSING',
                'REJECTED'
            )
        )
);

CREATE INDEX idx_claims_customer_id
    ON claims(customer_id);

CREATE INDEX idx_claims_policy_vehicle
    ON claims(policy_id, vehicle_id);

CREATE INDEX idx_claims_submitted_by
    ON claims(submitted_by_user_id);

CREATE INDEX idx_claims_status
    ON claims(status);

CREATE INDEX idx_claims_incident_date
    ON claims(incident_date);


-- 2. EVIDENCE
-- Stores metadata and storage references for uploaded files.
-- Actual files are stored in AWS S3.

CREATE TABLE evidence (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    claim_id BIGINT NOT NULL,

    uploaded_by_user_id BIGINT NOT NULL,

    file_name VARCHAR(255) NOT NULL,

    content_type VARCHAR(100) NOT NULL,

    file_size BIGINT NOT NULL,

    storage_provider VARCHAR(20) NOT NULL DEFAULT 'S3',

    s3_bucket VARCHAR(255),

    s3_object_key VARCHAR(1024),

    sha256_checksum VARCHAR(64),

    description TEXT,

    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_evidence_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_evidence_uploaded_by
        FOREIGN KEY (uploaded_by_user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_evidence_file_size
        CHECK (file_size > 0),

    CONSTRAINT chk_evidence_storage_provider
        CHECK (storage_provider IN ('S3', 'LOCAL')),

    CONSTRAINT chk_evidence_s3_location
        CHECK (
            storage_provider <> 'S3'
            OR (
                s3_bucket IS NOT NULL
                AND s3_object_key IS NOT NULL
            )
        )
);

CREATE INDEX idx_evidence_claim_id
    ON evidence(claim_id);

CREATE INDEX idx_evidence_uploaded_by
    ON evidence(uploaded_by_user_id);


-- 3. REPAIR INVOICES
-- Stores repair invoice information associated with evidence.

CREATE TABLE repair_invoices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    evidence_id BIGINT NOT NULL UNIQUE,

    invoice_number VARCHAR(100) NOT NULL,

    repairer_name VARCHAR(255) NOT NULL,

    invoice_date DATE NOT NULL,

    invoice_amount NUMERIC(14,2) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_repair_invoices_evidence
        FOREIGN KEY (evidence_id)
        REFERENCES evidence(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_invoice_amount
        CHECK (invoice_amount >= 0)
);

-- =====================================================
-- END OF V3 MIGRATION
-- =====================================================
