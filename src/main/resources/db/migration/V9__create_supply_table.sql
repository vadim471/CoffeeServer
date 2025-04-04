CREATE TABLE supply (
    id SERIAL PRIMARY KEY,
    vmc_number INTEGER NOT NULL,
    supply_id VARCHAR(255),
    russian_name VARCHAR(255),
    date_frame TIMESTAMP,
    surplus INTEGER
);