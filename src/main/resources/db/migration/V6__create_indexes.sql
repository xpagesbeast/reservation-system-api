CREATE INDEX idx_reservations_date ON reservations(reservation_date);
CREATE INDEX idx_reservations_customer ON reservations(customer_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_date_status ON reservations(reservation_date, status);
