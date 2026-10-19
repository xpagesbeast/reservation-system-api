CREATE TABLE reservation_system.lanes
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    lane_number       INT         NOT NULL,
    is_blocked        BIT(1)      NOT NULL,
    created_at        datetime NULL,
    updated_at        datetime NULL,
    maintenance_notes VARCHAR(500) NULL,
    status            VARCHAR(20) NOT NULL,
    physical_location VARCHAR(100) NULL,
    CONSTRAINT pk_lanes PRIMARY KEY (id)
);

ALTER TABLE reservation_system.lanes
    ADD CONSTRAINT uc_lanes_lanenumber UNIQUE (lane_number);