CREATE SCHEMA IF NOT EXISTS shipping_service;

CREATE TABLE IF NOT EXISTS shipping_service.shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    tracking_number VARCHAR(50) UNIQUE NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    shipping_method VARCHAR(50) NOT NULL,
    shipping_cost DECIMAL(10,2),
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    label_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipping_service.shipping_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL REFERENCES shipping_service.shipments(id),
    recipient_name VARCHAR(255) NOT NULL,
    address_data JSONB,
    phone VARCHAR(20),
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS shipping_service.tracking_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL REFERENCES shipping_service.shipments(id),
    status VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    description TEXT NOT NULL,
    event_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);