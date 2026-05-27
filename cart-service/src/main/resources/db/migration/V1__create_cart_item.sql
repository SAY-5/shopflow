CREATE TABLE cart_item (
    id           BIGSERIAL       PRIMARY KEY,
    session_id   VARCHAR(100)    NOT NULL,
    product_id   BIGINT          NOT NULL,
    product_name VARCHAR(200)    NOT NULL,
    unit_price   NUMERIC(12, 2)  NOT NULL,
    quantity     INTEGER         NOT NULL,
    CONSTRAINT uq_session_product UNIQUE (session_id, product_id),
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_cart_item_session ON cart_item (session_id);
