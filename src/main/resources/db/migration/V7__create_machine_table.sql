CREATE TABLE machine (
    id SERIAL PRIMARY KEY,
    vmc_number INTEGER NOT NULL UNIQUE,
    software_version VARCHAR(255),
    io_version VARCHAR(255)
);