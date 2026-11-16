-- ==========================================
-- V6: AUDIT LOGS
-- ==========================================

CREATE TABLE audit_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    actor_user_id BIGINT,

    action VARCHAR(100) NOT NULL,

    entity_type VARCHAR(50) NOT NULL,

    entity_id BIGINT NOT NULL,

    details JSONB,

    ip_address VARCHAR(45),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_audit_action
        CHECK (LENGTH(TRIM(action)) > 0),

    CONSTRAINT chk_audit_entity_type
        CHECK (LENGTH(TRIM(entity_type)) > 0)
);


-- Index for finding actions performed by a user
CREATE INDEX idx_audit_logs_actor
    ON audit_logs(actor_user_id);


-- Index for finding history of a particular entity
CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);


-- Index for retrieving recent audit events
CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at);
