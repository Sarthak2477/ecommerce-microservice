CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    variant_id UUID NOT NULL UNIQUE,
    product_id UUID NOT NULL,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    warehouse_location VARCHAR(255)
);

-- Seed data:
-- Example inventory records for testing (you can replace these UUIDs with actual values from productdb)
INSERT INTO inventory (id, variant_id, product_id, available_quantity, reserved_quantity, warehouse_location) VALUES
('b0849202-0e24-4ba2-8ef9-81358f276cd8', '47f7d540-df51-4171-be98-634a6a575a7c', 'b18d2ea2-e221-4328-971c-8ff1f64f4340', 100, 0, 'Warehouse-A1'),
('a3c898b5-cd5d-45df-b4df-ceea8db5c4a7', 'd32c56a8-bfb6-455f-8462-8178cd398321', '6a24be51-2495-46fd-ab7b-3ef1a69a5323', 50, 5, 'Warehouse-B2');
