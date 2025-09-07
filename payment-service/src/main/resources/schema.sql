CREATE SCHEMA IF NOT EXISTS payment_service;

CREATE TABLE IF NOT EXISTS payment_service.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100),
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_service.payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CARD', 'WALLET')),
    token VARCHAR(255) NOT NULL,
    last_four VARCHAR(4),
    brand VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_service.refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    order_id UUID NOT NULL,
    refund_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    refund_type VARCHAR(50) NOT NULL CHECK (refund_type IN ('FULL', 'PARTIAL', 'AUTOMATIC_CANCELLATION')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    reason TEXT,
    refund_transaction_id VARCHAR(100),
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payment_service.payments(id)
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payment_service.payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payment_service.payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_methods_customer_id ON payment_service.payment_methods(customer_id);
CREATE INDEX IF NOT EXISTS idx_refunds_payment_id ON payment_service.refunds(payment_id);
CREATE INDEX IF NOT EXISTS idx_refunds_order_id ON payment_service.refunds(order_id);
CREATE INDEX IF NOT EXISTS idx_refunds_status ON payment_service.refunds(status);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_methods_customer_default 
    ON payment_service.payment_methods(customer_id) 
    WHERE is_default = TRUE AND is_active = TRUE;