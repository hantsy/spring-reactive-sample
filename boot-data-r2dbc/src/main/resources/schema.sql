CREATE TABLE IF NOT EXISTS products (
    id SERIAL,
    name VARCHAR(200),
    price NUMERIC
);

ALTER TABLE products DROP CONSTRAINT IF EXISTS  products_pk;
ALTER TABLE products ADD CONSTRAINT products_pk PRIMARY KEY (id);