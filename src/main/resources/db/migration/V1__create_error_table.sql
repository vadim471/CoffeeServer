CREATE TABLE error (
    id BIGSERIAL AUTO_INCREMENT PRIMARY KEY,
    vmc_number INTEGER,
    fault_code VARCHAR(255),
    fault_info VARCHAR(255),
    occured_time TIMESTAMP,
    clear_time TIMESTAMP,
    fault_duration TIMESTAMP
);