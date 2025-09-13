CREATE SCHEMA order_service;

CREATE TABLE IF NOT EXISTS order_service.orders(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(30) UNIQUE,
    customer_id UUID,
    status VARCHAR(20) NOT NULL, -- PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED
    total_amount DECIMAL(10, 2),
    tax_amount DECIMAL(10, 2),
    shipping_amount DECIMAL(10, 2),
    shipping_address JSONB,
    -- Temporary payment fields (cleared after processing)
    payment_method_token VARCHAR(255) NULL,
    payment_method_type VARCHAR(50) NULL,
    -- Permanent payment fields (kept for records)
    payment_status VARCHAR(50) NULL,
    transaction_id VARCHAR(255) NULL,
    last_four_digits VARCHAR(4) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_service.order_items(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES order_service.orders(id),
    product_id UUID,
    product_name VARCHAR(255),
    quantity INTEGER,
    unit_price DECIMAL(10, 2),
    total_price DECIMAL(10, 2)
);

CREATE TABLE IF NOT EXISTS order_service.order_events(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES order_service.orders(id),
    event_type VARCHAR(50),
    event_payload JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);