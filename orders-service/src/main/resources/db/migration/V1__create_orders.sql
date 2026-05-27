CREATE TABLE customer_order (
    id           BIGSERIAL    PRIMARY KEY,
    customer_ref VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL
);

CREATE TABLE order_line (
    id           BIGSERIAL      PRIMARY KEY,
    order_id     BIGINT         NOT NULL REFERENCES customer_order (id) ON DELETE CASCADE,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    unit_price   NUMERIC(12, 2) NOT NULL,
    quantity     INTEGER        NOT NULL,
    CONSTRAINT chk_line_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_order_customer ON customer_order (customer_ref);
CREATE INDEX idx_order_line_order ON order_line (order_id);
