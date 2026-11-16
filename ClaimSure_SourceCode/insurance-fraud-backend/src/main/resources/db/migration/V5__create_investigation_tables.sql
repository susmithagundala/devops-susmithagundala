-- ==========================================
-- V5: INVESTIGATION MANAGEMENT TABLES
-- ==========================================

-- 1. INVESTIGATIONS

CREATE TABLE investigations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    claim_id BIGINT NOT NULL UNIQUE,
    investigator_user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_investigations_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_investigations_investigator
        FOREIGN KEY (investigator_user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_investigation_status
        CHECK (
            status IN (
                'OPEN',
                'WAITING_FOR_EVIDENCE',
                'COMPLETED'
            )
        ),

    CONSTRAINT chk_investigation_dates
        CHECK (
            completed_at IS NULL
            OR completed_at >= started_at
        )
);

CREATE INDEX idx_investigations_investigator
    ON investigations(investigator_user_id);

CREATE INDEX idx_investigations_status
    ON investigations(status);


-- 2. INVESTIGATION NOTES

CREATE TABLE investigation_notes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    investigation_id BIGINT NOT NULL,
    author_user_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_investigation_notes_investigation
        FOREIGN KEY (investigation_id)
        REFERENCES investigations(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_investigation_notes_author
        FOREIGN KEY (author_user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_investigation_note
        CHECK (LENGTH(TRIM(note)) > 0)
);

CREATE INDEX idx_investigation_notes_investigation
    ON investigation_notes(investigation_id);


-- 3. EVIDENCE REQUESTS

CREATE TABLE evidence_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    investigation_id BIGINT NOT NULL,
    requested_by_user_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMPTZ,
    fulfilled_at TIMESTAMPTZ,

    CONSTRAINT fk_evidence_requests_investigation
        FOREIGN KEY (investigation_id)
        REFERENCES investigations(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_evidence_requests_user
        FOREIGN KEY (requested_by_user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_evidence_request_status
        CHECK (
            status IN (
                'PENDING',
                'FULFILLED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_evidence_request_dates
        CHECK (
            fulfilled_at IS NULL
            OR fulfilled_at >= requested_at
        )
);

CREATE INDEX idx_evidence_requests_investigation
    ON evidence_requests(investigation_id);

CREATE INDEX idx_evidence_requests_status
    ON evidence_requests(status);


-- 4. CLAIM DECISIONS

CREATE TABLE claim_decisions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    claim_id BIGINT NOT NULL UNIQUE,
    investigation_id BIGINT NOT NULL,
    decided_by_user_id BIGINT NOT NULL,
    decision VARCHAR(30) NOT NULL,
    reason TEXT,
    decided_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_claim_decisions_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_claim_decisions_investigation
        FOREIGN KEY (investigation_id)
        REFERENCES investigations(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_claim_decisions_user
        FOREIGN KEY (decided_by_user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_claim_decision
        CHECK (
            decision IN (
                'CLEARED_FOR_PROCESSING',
                'REJECTED'
            )
        ),

    CONSTRAINT chk_claim_decision_reason
        CHECK (
            decision <> 'REJECTED'
            OR (
                reason IS NOT NULL
                AND LENGTH(TRIM(reason)) > 0
            )
        )
);

CREATE INDEX idx_claim_decisions_investigation
    ON claim_decisions(investigation_id);


-- 5. LINK UPLOADED EVIDENCE TO EVIDENCE REQUESTS

ALTER TABLE evidence
ADD COLUMN evidence_request_id BIGINT;

ALTER TABLE evidence
ADD CONSTRAINT fk_evidence_request
    FOREIGN KEY (evidence_request_id)
    REFERENCES evidence_requests(id)
    ON DELETE RESTRICT;

CREATE INDEX idx_evidence_request_id
    ON evidence(evidence_request_id);