CREATE TABLE supplies (
    id SERIAL PRIMARY KEY,
    supply_id INTEGER NOT NULL UNIQUE,
    chinese_name VARCHAR(255),
    russian_name VARCHAR(255)
);