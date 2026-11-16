-- =====================================================
-- V2: Create Insurance Tables
-- =====================================================

-- 1. VEHICLES

CREATE TABLE vehicles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    customer_id BIGINT NOT NULL,

    registration_number VARCHAR(20) NOT NULL UNIQUE,

    vin VARCHAR(50) NOT NULL UNIQUE,

    make VARCHAR(100) NOT NULL,

    model VARCHAR(100) NOT NULL,

    manufacturing_year INT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicles_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_vehicle_year
        CHECK (manufacturing_year BETWEEN 1886 AND 2100)
);

CREATE INDEX idx_vehicles_customer_id
    ON vehicles(customer_id);


-- 2. POLICIES

CREATE TABLE policies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    policy_number VARCHAR(50) NOT NULL UNIQUE,

    customer_id BIGINT NOT NULL,

    coverage_type VARCHAR(50) NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE NOT NULL,

    premium_amount NUMERIC(14,2) NOT NULL,

    coverage_limit NUMERIC(14,2) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_policies_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_policy_dates
        CHECK (end_date >= start_date),

    CONSTRAINT chk_policy_premium
        CHECK (premium_amount >= 0),

    CONSTRAINT chk_policy_coverage
        CHECK (coverage_limit >= 0),

    CONSTRAINT chk_policy_status
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_policies_customer_id
    ON policies(customer_id);


-- 3. POLICY VEHICLES
-- Junction table connecting policies and vehicles.

CREATE TABLE policy_vehicles (
    policy_id BIGINT NOT NULL,

    vehicle_id BIGINT NOT NULL,

    coverage_start_date DATE NOT NULL,

    coverage_end_date DATE NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_policy_vehicles_policy
        FOREIGN KEY (policy_id)
        REFERENCES policies(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_policy_vehicles_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_policy_vehicle
        UNIQUE (policy_id, vehicle_id),

    CONSTRAINT chk_vehicle_coverage_dates
        CHECK (coverage_end_date >= coverage_start_date)
);

CREATE INDEX idx_policy_vehicles_vehicle_id
    ON policy_vehicles(vehicle_id);

-- =====================================================
-- END OF V2 MIGRATION
-- =====================================================
