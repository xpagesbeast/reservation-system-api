package com.reservation_system.infrastructure.persistence;

import com.reservation_system.domain.Customer;
import com.reservation_system.domain.Reservation;
import com.reservation_system.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByCustomer(Customer customer);
    
    List<Reservation> findByCustomerEmail(String email);
    
    List<Reservation> findByReservationDate(LocalDate date);
    
    List<Reservation> findByReservationDateAndStatus(LocalDate date, ReservationStatus status);
    
    List<Reservation> findByStatus(ReservationStatus status);
    
    @Query("SELECT r FROM Reservation r WHERE r.customer.email = :email AND r.reservationDate = :date")
    List<Reservation> findByCustomerEmailAndReservationDate(@Param("email") String email, @Param("date") LocalDate date);
    
    @Query("SELECT r FROM Reservation r WHERE r.reservationTimeUtc < :endTime AND r.status = 'CONFIRMED'")
    List<Reservation> findConfirmedReservationsBefore(@Param("endTime") LocalDateTime endTime);
    
    @Query(value = "SELECT DISTINCT l.*, r.reservation_time_utc, r.duration_hours FROM reservation_lanes rl " +
           "JOIN reservations r ON rl.reservation_id = r.id " +
           "JOIN lanes l ON rl.lane_id = l.id " +
           "WHERE r.reservation_date = :date " +
           "AND r.status NOT IN ('CANCELLED', 'REJECTED', 'NO_SHOW') " +
           "AND r.reservation_time_utc < :endTime", 
           nativeQuery = true)
    List<Object[]> findOverlappingLanes(@Param("date") LocalDate date, 
                                        @Param("endTime") LocalDateTime endTime);
    
    @Query(value = "SELECT * FROM reservations WHERE YEAR(reservation_date) = :year", nativeQuery = true)
    List<Reservation> findByYear(@Param("year") int year);
    
    @Query(value = "SELECT * FROM reservations WHERE YEAR(reservation_date) = :year AND MONTH(reservation_date) = :month", nativeQuery = true)
    List<Reservation> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
    
    @Query(value = "SELECT * FROM reservations WHERE YEAR(reservation_date) = :year AND MONTH(reservation_date) = :month AND DAY(reservation_date) = :day", nativeQuery = true)
    List<Reservation> findByYearAndMonthAndDay(@Param("year") int year, @Param("month") int month, @Param("day") int day);
    
    List<Reservation> findByStatusIn(List<ReservationStatus> statuses);
}
