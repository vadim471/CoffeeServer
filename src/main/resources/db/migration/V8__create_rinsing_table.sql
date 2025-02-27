CREATE TABLE rinsing (
    id SERIAL PRIMARY KEY,
    vmc_number INTEGER NOT NULL UNIQUE,
    rinsing_code VARCHAR(255),
    c_uid VARCHAR(255),
    date_time TIMESTAMP,
    date_frame TIMESTAMP,
    is_ok BOOLEAN
);