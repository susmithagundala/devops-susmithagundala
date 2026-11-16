-- =====================================================
-- V1: Create Identity and RBAC Tables
-- Project: Insurance Claim Fraud Triage & Evidence Ledger
-- Database: PostgreSQL
-- =====================================================


-- 1. APPLICATION USERS
-- Stores login credentials and account information
-- for both claimants and investigators.

CREATE TABLE app_users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    email VARCHAR(255) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 2. ROLES
-- Stores the roles available in the application.

CREATE TABLE roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(50) NOT NULL UNIQUE,

    CONSTRAINT chk_roles_name
        CHECK (name IN ('CLAIMANT', 'INVESTIGATOR'))
);


-- 3. APP USER ROLES
-- Many-to-many relationship between app_users and roles.

CREATE TABLE app_user_roles (
    user_id BIGINT NOT NULL,

    role_id BIGINT NOT NULL,

    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_app_user_roles_role_id
    ON app_user_roles(role_id);


-- 4. CUSTOMERS
-- Stores additional profile information for claimants.
-- Investigators do not require customer profiles.

CREATE TABLE customers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    date_of_birth DATE,

    phone VARCHAR(20),

    address_line1 VARCHAR(255),

    address_line2 VARCHAR(255),

    city VARCHAR(100),

    state VARCHAR(100),

    postal_code VARCHAR(15),

    country VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT
);


-- 5. SEED INITIAL APPLICATION ROLES

INSERT INTO roles (name)
VALUES
    ('CLAIMANT'),
    ('INVESTIGATOR');


-- =====================================================
-- END OF V1 MIGRATION
-- =====================================================
