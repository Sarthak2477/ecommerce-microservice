CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    parent_id UUID,
    description TEXT,
    created_at TIMESTAMP
);

CREATE TABLE brands (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    logo_url TEXT,
    created_at TIMESTAMP
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    product_images UUID,
    product_variants UUID,
    product_attributes UUID,
    sku VARCHAR(100) UNIQUE,
    name VARCHAR(255),
    slug VARCHAR(255) UNIQUE,
    description TEXT,
    short_description TEXT,
    category_id UUID,
    brand_id UUID,
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    product_id UUID,
    image_url TEXT,
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0
);

CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID,
    sku VARCHAR(100) UNIQUE,
    variant_name VARCHAR(255),
    color VARCHAR(50),
    size VARCHAR(50),
    price DECIMAL(12,2),
    weight DECIMAL(10,2),
    created_at TIMESTAMP
);

CREATE TABLE product_attributes (
    id UUID PRIMARY KEY,
    product_id UUID,
    attribute_name VARCHAR(100),
    attribute_value VARCHAR(255)
);