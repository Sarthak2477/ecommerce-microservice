\c orderdb;

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Seed data for testing:
-- Inserts a sample order and its items (using INSERT INTO ... ON CONFLICT / ignore or simple INSERT)
INSERT INTO orders (id, user_id, status, total_amount) 
VALUES ('5e2f67fa-b3d4-4a3c-b93c-7e02792177b4', 'a3c898b5-cd5d-45df-b4df-ceea8db5c4a7', 'PENDING', 299.98)
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_items (id, order_id, product_id, quantity, price, subtotal) 
VALUES ('7b8c8759-d59e-40bf-b86b-fbb524cdf0d0', '5e2f67fa-b3d4-4a3c-b93c-7e02792177b4', '47f7d540-df51-4171-be98-634a6a575a7c', 2, 149.99, 299.98)
ON CONFLICT (id) DO NOTHING;
