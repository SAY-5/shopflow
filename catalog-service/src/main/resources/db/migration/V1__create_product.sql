CREATE TABLE product (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    description     VARCHAR(1000)   NOT NULL,
    price           NUMERIC(12, 2)  NOT NULL,
    available_units INTEGER         NOT NULL,
    reserved_units  INTEGER         NOT NULL DEFAULT 0,
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_reserved_nonneg CHECK (reserved_units >= 0),
    CONSTRAINT chk_reserved_le_available CHECK (reserved_units <= available_units)
);
