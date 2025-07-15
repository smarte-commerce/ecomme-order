-- =============================================
-- ORDER SERVICE DATABASE SCHEMA
-- =============================================

-- Create database
CREATE DATABASE order_service;
USE order_service;

-- Orders table
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    amount_to_be_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM('draft', 'pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') DEFAULT 'draft',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    shipping_address JSON,
    billing_address JSON,
    estimated_delivery_date DATE,
    special_instructions TEXT,
    
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_number (order_number),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_updated_at (updated_at)
);

-- Order items table
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100),
    product_category VARCHAR(100),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    weight DECIMAL(8,2) DEFAULT 0.00,
    dimensions JSON, -- {"length": 10, "width": 8, "height": 5, "unit": "cm"}
    tax_category VARCHAR(50) DEFAULT 'standard',
    status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_status (status),
    INDEX idx_tax_category (tax_category)
);

-- Order discounts table
CREATE TABLE order_discounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    discount_code VARCHAR(100) NOT NULL,
    discount_name VARCHAR(255),
    discount_type ENUM('percentage', 'fixed_amount', 'free_shipping', 'buy_x_get_y') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL CHECK (discount_amount >= 0),
    applies_to ENUM('order', 'shipping', 'item') DEFAULT 'order',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_discount_code (discount_code),
    INDEX idx_applies_to (applies_to)
);

-- Order status history table
CREATE TABLE order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    old_status ENUM('draft', 'pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded'),
    new_status ENUM('draft', 'pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') NOT NULL,
    reason TEXT,
    notes TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_changed_at (changed_at)
);

-- Vendor orders table (for multi-vendor fulfillment)
CREATE TABLE vendor_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    vendor_order_number VARCHAR(50) UNIQUE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    commission_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    commission_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    vendor_payout DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_status (status),
    INDEX idx_vendor_order_number (vendor_order_number)
);

-- Order external references (for linking to other services)
CREATE TABLE order_external_refs (
            id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    service_name VARCHAR(50) NOT NULL,
    external_id VARCHAR(100) NOT NULL,
    ref_type VARCHAR(50) NOT NULL, -- 'payment', 'shipping', 'tax_calculation', etc.
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_service_name (service_name),
    INDEX idx_ref_type (ref_type),
    INDEX idx_external_id (external_id)
);

-- =============================================
-- SAMPLE DATA FOR ORDER SERVICE
-- =============================================

-- Sample orders
INSERT INTO orders (customer_id, order_number, subtotal, discount_amount, tax_amount, shipping_amount, total_amount, status, shipping_address, billing_address, estimated_delivery_date, special_instructions) VALUES
(1001, 'ORD-2024-001', 150.00, 15.00, 12.60, 9.99, 157.59, 'confirmed', 
 '{"street": "123 Main St", "city": "New York", "state": "NY", "zip": "10001", "country": "US"}',
 '{"street": "123 Main St", "city": "New York", "state": "NY", "zip": "10001", "country": "US"}',
 '2024-07-08',
 'Customer requested fast delivery'),
(1002, 'ORD-2024-002', 89.97, 0.00, 7.20, 5.99, 103.16, 'processing',
 '{"street": "456 Oak Ave", "city": "Los Angeles", "state": "CA", "zip": "90210", "country": "US"}',
 '{"street": "456 Oak Ave", "city": "Los Angeles", "state": "CA", "zip": "90210", "country": "US"}',
 '2024-07-10',
 NULL),
(1003, 'ORD-2024-003', 299.97, 30.00, 21.60, 0.00, 291.57, 'shipped',
 '{"street": "789 Pine Rd", "city": "Chicago", "state": "IL", "zip": "60601", "country": "US"}',
 '{"street": "789 Pine Rd", "city": "Chicago", "state": "IL", "zip": "60601", "country": "US"}',
 '2024-07-09',
 'Free shipping applied');

-- Sample order items
INSERT INTO order_items (order_id, product_id, vendor_id, product_name, product_sku, product_category, quantity, unit_price, total_price, weight, dimensions, tax_category, status) VALUES
(1, 2001, 3001, 'Wireless Headphones', 'WH-1000XM4', 'Electronics', 1, 150.00, 150.00, 0.25, '{"length": 20, "width": 15, "height": 8, "unit": "cm"}', 'standard', 'confirmed'),
(2, 2002, 3002, 'Smartphone Case', 'SC-iPhone14', 'Accessories', 2, 24.99, 49.98, 0.05, '{"length": 15, "width": 8, "height": 1, "unit": "cm"}', 'standard', 'processing'),
(2, 2003, 3001, 'USB Cable', 'USB-C-2M', 'Electronics', 2, 19.99, 39.98, 0.10, '{"length": 200, "width": 2, "height": 2, "unit": "cm"}', 'standard', 'processing'),
(3, 2004, 3003, 'Laptop Stand', 'LS-ALU-ADJ', 'Office', 1, 89.99, 89.99, 0.80, '{"length": 25, "width": 20, "height": 5, "unit": "cm"}', 'standard', 'shipped'),
(3, 2005, 3003, 'Mechanical Keyboard', 'MK-RGB-TKL', 'Electronics', 1, 139.99, 139.99, 0.90, '{"length": 35, "width": 15, "height": 3, "unit": "cm"}', 'standard', 'shipped'),
(3, 2006, 3001, 'Wireless Mouse', 'WM-2.4G-RGB', 'Electronics', 1, 69.99, 69.99, 0.12, '{"length": 12, "width": 6, "height": 4, "unit": "cm"}', 'standard', 'shipped');

-- Sample order discounts
INSERT INTO order_discounts (order_id, discount_code, discount_name, discount_type, discount_value, discount_amount, applies_to) VALUES
(1, 'SAVE10', '10% Off Electronics', 'percentage', 10.00, 15.00, 'order'),
(3, 'FREESHIP', 'Free Shipping Over $200', 'free_shipping', 0.00, 9.99, 'shipping'),
(3, 'WELCOME20', 'Welcome 20% Off', 'percentage', 20.00, 20.01, 'order');

-- Sample order status history
INSERT INTO order_status_history (order_id, old_status, new_status, reason, changed_by) VALUES
(1, 'draft', 'pending', 'Cart checkout completed', 'system'),
(1, 'pending', 'confirmed', 'Payment confirmed', 'system'),
(2, 'draft', 'pending', 'Cart checkout completed', 'system'),
(2, 'pending', 'confirmed', 'Payment confirmed', 'system'),
(2, 'confirmed', 'processing', 'Order sent to vendors', 'system'),
(3, 'draft', 'pending', 'Cart checkout completed', 'system'),
(3, 'pending', 'confirmed', 'Payment confirmed', 'system'),
(3, 'confirmed', 'processing', 'Order sent to vendors', 'system'),
(3, 'processing', 'shipped', 'All items shipped', 'system');

-- Sample vendor orders
INSERT INTO vendor_orders (order_id, vendor_id, vendor_order_number, subtotal, commission_rate, commission_amount, vendor_payout, status) VALUES
(1, 3001, 'VO-3001-001', 150.00, 10.00, 15.00, 135.00, 'confirmed'),
(2, 3002, 'VO-3002-001', 49.98, 12.00, 6.00, 43.98, 'processing'),
(2, 3001, 'VO-3001-002', 39.98, 10.00, 4.00, 35.98, 'processing'),
(3, 3003, 'VO-3003-001', 229.98, 8.00, 18.40, 211.58, 'shipped'),
(3, 3001, 'VO-3001-003', 69.99, 10.00, 7.00, 62.99, 'shipped');

-- Sample external references
INSERT INTO order_external_refs (order_id, service_name, external_id, ref_type, status) VALUES
(1, 'payment_service', 'PAY-2024-001', 'payment', 'completed'),
(1, 'shipping_service', 'SHIP-2024-001', 'shipping', 'delivered'),
(1, 'tax_service', 'TAX-2024-001', 'tax_calculation', 'completed'),
(2, 'payment_service', 'PAY-2024-002', 'payment', 'completed'),
(2, 'shipping_service', 'SHIP-2024-002', 'shipping', 'in_transit'),
(2, 'tax_service', 'TAX-2024-002', 'tax_calculation', 'completed'),
(3, 'payment_service', 'PAY-2024-003', 'payment', 'completed'),
(3, 'shipping_service', 'SHIP-2024-003', 'shipping', 'delivered'),
(3, 'tax_service', 'TAX-2024-003', 'tax_calculation', 'completed');

-- =============================================
-- USEFUL QUERIES FOR ORDER SERVICE
-- =============================================

-- Get order details with items, discounts, and external references
SELECT 
    o.order_number,
    o.status,
    o.total_amount,
    o.estimated_delivery_date,
    oi.product_name,
    oi.quantity,
    oi.unit_price,
    od.discount_code,
    od.discount_amount,
    oer.service_name,
    oer.external_id,
    oer.ref_type
FROM orders o
LEFT JOIN order_items oi ON o.order_id = oi.order_id
LEFT JOIN order_discounts od ON o.order_id = od.order_id
LEFT JOIN order_external_refs oer ON o.order_id = oer.order_id
WHERE o.order_id = 1;

-- Get vendor order summary with item details
SELECT 
    vo.vendor_order_number,
    vo.vendor_id,
    vo.subtotal,
    vo.commission_amount,
    vo.vendor_payout,
    vo.status,
    COUNT(oi.order_item_id) as item_count,
    SUM(oi.weight) as total_weight
FROM vendor_orders vo
LEFT JOIN order_items oi ON vo.order_id = oi.order_id AND vo.vendor_id = oi.vendor_id
GROUP BY vo.vendor_order_id;

-- Get orders by customer with delivery status
SELECT 
    o.order_number,
    o.order_date,
    o.status,
    o.total_amount,
    o.estimated_delivery_date,
    COUNT(oi.order_item_id) as item_count,
    SUM(oi.weight) as total_weight
FROM orders o
LEFT JOIN order_items oi ON o.order_id = oi.order_id
WHERE o.customer_id = 1001
GROUP BY o.order_id
ORDER BY o.order_date DESC;

-- Get order status changes history
SELECT 
    o.order_number,
    osh.old_status,
    osh.new_status,
    osh.reason,
    osh.notes,
    osh.changed_at,
    osh.changed_by
FROM orders o
JOIN order_status_history osh ON o.order_id = osh.order_id
WHERE o.order_id = 1
ORDER BY osh.changed_at;

-- Get orders by vendor with external service references
SELECT 
    v.vendor_id,
    v.vendor_order_number,
    o.order_number,
    v.subtotal,
    v.status,
    v.created_at,
    GROUP_CONCAT(CONCAT(oer.service_name, ':', oer.external_id) SEPARATOR ', ') as external_refs
FROM vendor_orders v
JOIN orders o ON v.order_id = o.order_id
LEFT JOIN order_external_refs oer ON o.order_id = oer.order_id
WHERE v.vendor_id = 3001
GROUP BY v.vendor_order_id
ORDER BY v.created_at DESC;
