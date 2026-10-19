package com.reservation_system.infrastructure.persistence;

import com.reservation_system.domain.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {
    
    List<Lane> findByIsBlockedFalse();
    
    List<Lane> findByIsBlockedTrue();
    
    @Query("SELECT l FROM Lane l WHERE l.isBlocked = false AND l.id NOT IN " +
           "(SELECT l2.id FROM Reservation r JOIN r.lanes l2 WHERE " +
           "r.reservationDate = :date AND " +
           "r.status NOT IN ('CANCELLED', 'REJECTED', 'NO_SHOW'))")
    List<Lane> findAvailableLanesForDate(java.time.LocalDate date);
    
    @Query(value = "SELECT DISTINCT l.id, l.lane_number, r.reservation_time_utc, r.duration_hours, c.name as customer_name " +
           "FROM lanes l " +
           "JOIN reservation_lanes rl ON l.id = rl.lane_id " +
           "JOIN reservations r ON rl.reservation_id = r.id " +
           "JOIN customers c ON r.customer_id = c.id " +
           "WHERE r.status NOT IN ('CANCELLED', 'REJECTED', 'NO_SHOW') " +
           "AND r.reservation_time_utc <= :hourEnd " +
           "ORDER BY l.lane_number", 
           nativeQuery = true)
    List<Object[]> findReservedLanesForCurrentHour(@Param("hourEnd") LocalDateTime hourEnd);

    default List<Lane> getAllLanes(){
        return findAll();
    }

    @Query(value = "SELECT * FROM lanes l WHERE l.lane_Number = :laneNumber", nativeQuery = true)
    Optional<Lane> findByLaneNumber(Long laneNumber);
}
