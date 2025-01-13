CREATE TABLE error (
    id BIGSERIAL PRIMARY KEY,
    vmc_number INTEGER,
    fault_code INTEGER,
    fault_info VARCHAR(255),
    occured_time TIMESTAMP,
    clear_time TIMESTAMP NULL,
    fault_duration INTERVAL NULL,
    UNIQUE (vmc_number, fault_code)
);