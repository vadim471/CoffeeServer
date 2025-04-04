CREATE TABLE machine_activity (
    id SERIAL PRIMARY KEY,
    vmc_number INTEGER NOT NULL,
    last_message VARCHAR(255),
    date_last_active TIMESTAMP
);