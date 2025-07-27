INSERT INTO inventory_service.products (id, sku, name, description, price, category, image_url, active) VALUES
('550e8400-e29b-41d4-a716-446655440201', 'WBH-001', 'Wireless Bluetooth Headphones', 'Premium noise-canceling wireless headphones with 30-hour battery life', 79.99, 'Electronics', 'https://example.com/images/headphones.jpg', true),
('550e8400-e29b-41d4-a716-446655440202', 'USB-C-6FT', 'USB-C Cable 6ft', 'High-speed USB-C to USB-C cable for charging and data transfer', 19.99, 'Accessories', 'https://example.com/images/usb-cable.jpg', true),
('550e8400-e29b-41d4-a716-446655440203', 'PC-SILICONE', 'Phone Case', 'Durable silicone phone case with drop protection', 24.99, 'Accessories', 'https://example.com/images/phone-case.jpg', true),
('550e8400-e29b-41d4-a716-446655440204', 'SP-GLASS-3PK', 'Screen Protector Pack', 'Tempered glass screen protectors - 3 pack', 14.99, 'Accessories', 'https://example.com/images/screen-protector.jpg', true),
('550e8400-e29b-41d4-a716-446655440205', 'WC-QI-15W', 'Wireless Charger', '15W fast wireless charging pad with LED indicator', 49.99, 'Electronics', 'https://example.com/images/wireless-charger.jpg', true),
('550e8400-e29b-41d4-a716-446655440206', 'LS-ALUMINUM', 'Laptop Stand', 'Adjustable aluminum laptop stand for ergonomic viewing', 89.99, 'Office', 'https://example.com/images/laptop-stand.jpg', true),
('550e8400-e29b-41d4-a716-446655440207', 'KB-MECH-RGB', 'Mechanical Keyboard', 'RGB backlit mechanical keyboard with blue switches', 109.99, 'Office', 'https://example.com/images/keyboard.jpg', true),
('550e8400-e29b-41d4-a716-446655440208', 'MP-EXTENDED', 'Mouse Pad', 'Extended gaming mouse pad with non-slip base', 19.99, 'Office', 'https://example.com/images/mouse-pad.jpg', true),
('550e8400-e29b-41d4-a716-446655440209', 'WC-HD-1080P', 'Webcam HD', '1080P HD webcam with built-in microphone', 39.99, 'Electronics', 'https://example.com/images/webcam.jpg', true),
('550e8400-e29b-41d4-a716-446655440210', 'BS-PORTABLE', 'Bluetooth Speaker', 'Portable waterproof Bluetooth speaker with 12-hour battery', 39.99, 'Electronics', 'https://example.com/images/speaker.jpg', true),
('550e8400-e29b-41d4-a716-446655440211', 'SW-FITNESS', 'Smart Watch', 'Fitness tracking smart watch with heart rate monitor', 149.99, 'Electronics', 'https://example.com/images/smartwatch.jpg', true),
('550e8400-e29b-41d4-a716-446655440212', 'TB-SSD-1TB', 'External SSD 1TB', 'Portable 1TB SSD with USB 3.2 Gen 2 connectivity', 89.99, 'Storage', 'https://example.com/images/external-ssd.jpg', true);

INSERT INTO inventory_service.inventory (product_id, available_quantity, reserved_quantity) VALUES
('550e8400-e29b-41d4-a716-446655440201', 25, 0),  -- Headphones
('550e8400-e29b-41d4-a716-446655440202', 50, 0),  -- USB Cable
('550e8400-e29b-41d4-a716-446655440203', 30, 0),  -- Phone Case
('550e8400-e29b-41d4-a716-446655440204', 40, 0),  -- Screen Protector
('550e8400-e29b-41d4-a716-446655440205', 15, 0),  -- Wireless Charger
('550e8400-e29b-41d4-a716-446655440206', 20, 0),  -- Laptop Stand
('550e8400-e29b-41d4-a716-446655440207', 12, 0),  -- Mechanical Keyboard
('550e8400-e29b-41d4-a716-446655440208', 35, 0),  -- Mouse Pad
('550e8400-e29b-41d4-a716-446655440209', 18, 0),  -- Webcam
('550e8400-e29b-41d4-a716-446655440210', 22, 0),  -- Bluetooth Speaker
('550e8400-e29b-41d4-a716-446655440211', 8, 0),
('550e8400-e29b-41d4-a716-446655440212', 10, 0);  -- External SSD