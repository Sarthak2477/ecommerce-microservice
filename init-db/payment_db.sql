\c paymentdb;

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


