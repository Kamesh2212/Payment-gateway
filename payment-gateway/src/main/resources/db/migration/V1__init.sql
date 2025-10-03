-- ==============================================================
--  Flyway Migration: V1__init.sql
--  Initial schema for Payment Gateway (updated)
-- ==============================================================

-- ==============
-- payments table
-- ==============
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_ref VARCHAR(255) NOT NULL UNIQUE,
    account_number VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    currency VARCHAR(3) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0       -- for @Version
);

-- ======================
-- idempotency_keys table
-- ======================
CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    key_value VARCHAR(200) NOT NULL UNIQUE,
    payment_ref VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX ux_idem_key ON idempotency_keys (key_value);

-- ==========================
-- payment_status_history table
-- ==========================
CREATE TABLE payment_status_history (
    id BIGSERIAL PRIMARY KEY,
    payment_ref VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    reason VARCHAR(255),
    changed_at TIMESTAMP NOT NULL,
    delta_amount NUMERIC(19,2) NOT NULL DEFAULT 0  -- refund deltas
);

CREATE INDEX ix_psh_paymentref_changedat
  ON payment_status_history (payment_ref, changed_at);
