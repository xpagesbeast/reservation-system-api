CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time_utc TIMESTAMP NOT NULL,
    duration_hours INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    extended_hours_requested BOOLEAN NOT NULL DEFAULT FALSE,
    extended_hours_approved BOOLEAN NOT NULL DEFAULT FALSE,
    cancellation_policy_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    note TEXT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);
