-- ==========================================
-- V4: FRAUD DETECTION TABLES
-- ==========================================

-- 1. FRAUD RULE CONFIGURATIONS

CREATE TABLE fraud_rule_configurations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rule_code VARCHAR(20) NOT NULL UNIQUE,
    rule_name VARCHAR(150) NOT NULL,
    description TEXT,
    weight INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_fraud_rule_weight
        CHECK (weight BETWEEN 0 AND 100)
);


-- 2. CLAIM FLAGS

CREATE TABLE claim_flags (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    claim_id BIGINT NOT NULL,
    rule_configuration_id BIGINT NOT NULL,
    rule_code_snapshot VARCHAR(20) NOT NULL,
    score_contribution INTEGER NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_claim_flags_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_claim_flags_rule
        FOREIGN KEY (rule_configuration_id)
        REFERENCES fraud_rule_configurations(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_claim_rule_flag
        UNIQUE (claim_id, rule_configuration_id),

    CONSTRAINT chk_claim_flag_score
        CHECK (score_contribution >= 0)
);

CREATE INDEX idx_claim_flags_claim_id
    ON claim_flags(claim_id);

CREATE INDEX idx_claim_flags_rule_id
    ON claim_flags(rule_configuration_id);


-- 3. ENTITY MATCHES

CREATE TABLE entity_matches (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    claim_id BIGINT NOT NULL,
    matched_claim_id BIGINT NOT NULL,
    match_type VARCHAR(50) NOT NULL,
    confidence_score NUMERIC(5,4),
    match_details JSONB,
    review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_entity_matches_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_entity_matches_matched_claim
        FOREIGN KEY (matched_claim_id)
        REFERENCES claims(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_entity_matches_different_claims
        CHECK (claim_id <> matched_claim_id),

    CONSTRAINT chk_entity_match_confidence
        CHECK (
            confidence_score IS NULL
            OR confidence_score BETWEEN 0 AND 1
        ),

    CONSTRAINT chk_entity_match_type
        CHECK (
            match_type IN (
                'DUPLICATE_CLAIM',
                'SAME_VEHICLE',
                'SAME_CUSTOMER',
                'REPEATED_INVOICE',
                'SAME_REPAIRER',
                'OTHER'
            )
        ),

    CONSTRAINT chk_entity_match_review_status
        CHECK (
            review_status IN (
                'PENDING',
                'CONFIRMED',
                'DISMISSED'
            )
        ),

    CONSTRAINT uq_entity_match
        UNIQUE (claim_id, matched_claim_id, match_type)
);

CREATE INDEX idx_entity_matches_claim_id
    ON entity_matches(claim_id);

CREATE INDEX idx_entity_matches_matched_claim_id
    ON entity_matches(matched_claim_id);

CREATE INDEX idx_entity_matches_review_status
    ON entity_matches(review_status);
