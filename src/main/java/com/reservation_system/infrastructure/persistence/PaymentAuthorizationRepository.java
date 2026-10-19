package com.reservation_system.infrastructure.persistence;

import com.reservation_system.domain.PaymentAuthorization;
import com.reservation_system.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAuthorizationRepository extends JpaRepository<PaymentAuthorization, Long> {
    
    List<PaymentAuthorization> findByReservation(Reservation reservation);
    
    Optional<PaymentAuthorization> findFirstByReservationOrderByCreatedAtDesc(Reservation reservation);
}
