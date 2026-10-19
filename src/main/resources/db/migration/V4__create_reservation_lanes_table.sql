CREATE TABLE reservation_lanes (
    reservation_id BIGINT NOT NULL,
    lane_id BIGINT NOT NULL,
    PRIMARY KEY (reservation_id, lane_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    FOREIGN KEY (lane_id) REFERENCES lanes(id)
);
