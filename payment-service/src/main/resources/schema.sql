-- Payment Service Database Schema
CREATE SCHEMA IF NOT EXISTS payment_service;

-- Payments table - Core payment processing records
CREATE TABLE IF NOT EXISTS payment_service.payments (
                                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL, -- PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED
    payment_method VARCHAR(50) NOT NULL, -- CREDIT_CARD, DEBIT_CARD, PAYPAL, APPLE_PAY, GOOGLE_PAY
    transaction_id VARCHAR(100), -- External payment gateway transaction ID
    gateway_response JSONB, -- Store full gateway response for debugging
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Payment methods table - Store customer payment methods securely
CREATE TABLE IF NOT EXISTS payment_service.payment_methods (
                                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL, -- CARD, WALLET
    token VARCHAR(255) NOT NULL, -- Encrypted payment token from gateway
    last_four VARCHAR(4), -- Last 4 digits for display
    brand VARCHAR(20), -- VISA, MASTERCARD, AMEX, etc.
    expiry_month INTEGER,
    expiry_year INTEGER,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Payment attempts table - Track retry attempts for failed payments
CREATE TABLE IF NOT EXISTS payment_service.payment_attempts (
                                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payment_service.payments(id),
    attempt_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED
    gateway_transaction_id VARCHAR(100),
    gateway_response JSONB,
    failure_reason TEXT,
    processing_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Refunds table - Track refund transactions
CREATE TABLE IF NOT EXISTS payment_service.refunds (
                                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payment_service.payments(id),
    order_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED
    refund_reason VARCHAR(100),
    gateway_refund_id VARCHAR(100),
    gateway_response JSONB,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Payment events table - Store payment-related events for audit and debugging
CREATE TABLE IF NOT EXISTS payment_service.payment_events (
                                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID REFERENCES payment_service.payments(id),
    event_type VARCHAR(50) NOT NULL,
    event_payload JSONB NOT NULL,
    correlation_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Idempotency table - Prevent duplicate payment processing
CREATE TABLE IF NOT EXISTS payment_service.idempotency_keys (
                                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    payment_id UUID REFERENCES payment_service.payments(id),
    request_hash VARCHAR(64) NOT NULL, -- SHA-256 hash of request body
    response_payload JSONB,
    status VARCHAR(20) NOT NULL, -- PROCESSING, COMPLETED, FAILED
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Webhook events table - Store incoming webhook events from payment gateways
CREATE TABLE IF NOT EXISTS payment_service.webhook_events (
                                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    webhook_id VARCHAR(100) UNIQUE NOT NULL, -- Webhook ID from gateway
    gateway_name VARCHAR(50) NOT NULL, -- STRIPE, PAYPAL, etc.
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    processing_attempts INTEGER DEFAULT 0,
    last_processing_error TEXT,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
    );

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payment_service.payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_customer_id ON payment_service.payments(customer_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payment_service.payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payment_service.payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payment_service.payments(transaction_id);

CREATE INDEX IF NOT EXISTS idx_payment_methods_customer_id ON payment_service.payment_methods(customer_id);
CREATE INDEX IF NOT EXISTS idx_payment_methods_is_default ON payment_service.payment_methods(customer_id, is_default) WHERE is_default = TRUE;

CREATE INDEX IF NOT EXISTS idx_payment_attempts_payment_id ON payment_service.payment_attempts(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_attempts_status ON payment_service.payment_attempts(status);

CREATE INDEX IF NOT EXISTS idx_refunds_payment_id ON payment_service.refunds(payment_id);
CREATE INDEX IF NOT EXISTS idx_refunds_order_id ON payment_service.refunds(order_id);
CREATE INDEX IF NOT EXISTS idx_refunds_status ON payment_service.refunds(status);

CREATE INDEX IF NOT EXISTS idx_payment_events_payment_id ON payment_service.payment_events(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_events_event_type ON payment_service.payment_events(event_type);
CREATE INDEX IF NOT EXISTS idx_payment_events_correlation_id ON payment_service.payment_events(correlation_id);

CREATE INDEX IF NOT EXISTS idx_idempotency_keys_expires_at ON payment_service.idempotency_keys(expires_at);

CREATE INDEX IF NOT EXISTS idx_webhook_events_processed ON payment_service.webhook_events(processed);
CREATE INDEX IF NOT EXISTS idx_webhook_events_gateway_name ON payment_service.webhook_events(gateway_name);
CREATE INDEX IF NOT EXISTS idx_webhook_events_received_at ON payment_service.webhook_events(received_at);

-- Constraints
ALTER TABLE payment_service.payment_methods
    ADD CONSTRAINT chk_payment_method_type CHECK (type IN ('CARD', 'WALLET'));

ALTER TABLE payment_service.payments
    ADD CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'));

ALTER TABLE payment_service.payments
    ADD CONSTRAINT chk_payment_method CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'APPLE_PAY', 'GOOGLE_PAY'));

ALTER TABLE payment_service.refunds
    ADD CONSTRAINT chk_refund_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'));

ALTER TABLE payment_service.idempotency_keys
    ADD CONSTRAINT chk_idempotency_status CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'));

-- Ensure only one default payment method per customer
CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_methods_customer_default
    ON payment_service.payment_methods(customer_id)
    WHERE is_default = TRUE AND is_active = TRUE;

-- Note: PostgreSQL functions with $ syntax don't work well with Spring Boot's SQL script runner
-- Alternative: Use application-level timestamp updates or create the function manually in the database
-- For now, we'll rely on application-level updates for updated_at fields

-- If you need the triggers, run these commands manually in your PostgreSQL database:
/*
CREATE OR REPLACE FUNCTION payment_service.update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

CREATE TRIGGER update_payments_updated_at 
    BEFORE UPDATE ON payment_service.payments 
    FOR EACH ROW EXECUTE FUNCTION payment_service.update_updated_at_column();

CREATE TRIGGER update_payment_methods_updated_at 
    BEFORE UPDATE ON payment_service.payment_methods 
    FOR EACH ROW EXECUTE FUNCTION payment_service.update_updated_at_column();

CREATE TRIGGER update_refunds_updated_at 
    BEFORE UPDATE ON payment_service.refunds 
    FOR EACH ROW EXECUTE FUNCTION payment_service.update_updated_at_column();
*/