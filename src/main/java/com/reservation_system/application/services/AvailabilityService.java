package com.reservation_system.application.services;

import com.reservation_system.application.OperatingHoursService;
import com.reservation_system.application.TimeZoneService;
import com.reservation_system.domain.Lane;
import com.reservation_system.domain.Reservation;
import com.reservation_system.domain.enums.ReservationStatus;
import com.reservation_system.infrastructure.persistence.LaneRepository;
import com.reservation_system.infrastructure.persistence.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {
    
    private final LaneRepository laneRepository;
    private final ReservationRepository reservationRepository;
    private final OperatingHoursService operatingHoursService;
    private final TimeZoneService timeZoneService;
    private final int bufferTimeMinutes;
    
    public AvailabilityService(LaneRepository laneRepository,
                              ReservationRepository reservationRepository,
                              OperatingHoursService operatingHoursService,
                              TimeZoneService timeZoneService,
                              @Value("${reservation.buffer-time-minutes}") int bufferTimeMinutes) {
        this.laneRepository = laneRepository;
        this.reservationRepository = reservationRepository;
        this.operatingHoursService = operatingHoursService;
        this.timeZoneService = timeZoneService;
        this.bufferTimeMinutes = bufferTimeMinutes;
    }
    
    public List<Lane> getAvailableLanes(LocalDate date, LocalTime time, int durationHours) {
        // Convert local time to UTC for database queries
        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        LocalDateTime utcDateTime = timeZoneService.convertAspenToUtc(localDateTime);
        
        // Validate operating hours
        if (!operatingHoursService.isWithinOperatingHours(date, time)) {
            return List.of();
        }
        
        // Calculate time range with buffer
        LocalDateTime startTime = utcDateTime.minusMinutes(bufferTimeMinutes);
        LocalDateTime endTime = utcDateTime.plusHours(durationHours).plusMinutes(bufferTimeMinutes);
        
        // Get all non-blocked lanes
        List<Lane> allLanes = laneRepository.findByIsBlockedFalse();
        
        // Find overlapping reservations (returns Object[] with lane data + time info)
        List<Object[]> overlappingLanesData = reservationRepository.findOverlappingLanes(date, endTime);
        
        // Filter in Java to only include reservations that extend past start time
        // This replaces the SQL DATE_ADD arithmetic for database compatibility
        Set<Long> overlappingLaneIds = overlappingLanesData.stream()
            .filter(row -> {
                LocalDateTime reservationStartUtc = (LocalDateTime) row[5]; // reservation_time_utc
                int reservationDuration = (int) row[6]; // duration_hours
                LocalDateTime reservationEndUtc = reservationStartUtc.plusHours(reservationDuration);
                return reservationEndUtc.isAfter(startTime);
            })
            .map(row -> (Long) row[0]) // lane id
            .collect(Collectors.toSet());
        
        return allLanes.stream()
                .filter(lane -> !overlappingLaneIds.contains(lane.getId()))
                .collect(Collectors.toList());
    }
    
    public boolean isLaneAvailable(Long laneId, LocalDate date, LocalTime time, int durationHours) {
        List<Lane> availableLanes = getAvailableLanes(date, time, durationHours);
        return availableLanes.stream().anyMatch(lane -> lane.getId().equals(laneId));
    }
    
    /**
     * Get lane status for the current hour in Aspen time zone.
     * Returns available and reserved lanes with reservation details.
     * 
     * @return List of Object arrays containing lane ID, lane number, reservation end time (UTC),
     *         duration hours, and customer name for reserved lanes
     */
    public List<Object[]> getLaneStatusForCurrentHour() {
        // Get current time in Aspen
        LocalDateTime nowUtc = LocalDateTime.now();
        LocalDateTime nowAspen = timeZoneService.convertUtcToAspen(nowUtc);
        
        // Calculate current hour start and end in UTC
        LocalDateTime hourStartAspen = nowAspen.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime hourEndAspen = hourStartAspen.plusHours(1);
        
        LocalDateTime hourStartUtc = timeZoneService.convertAspenToUtc(hourStartAspen);
        LocalDateTime hourEndUtc = timeZoneService.convertAspenToUtc(hourEndAspen);
        
        // Get all reservations that start before or at hour end
        List<Object[]> allReservedLanes = laneRepository.findReservedLanesForCurrentHour(hourEndUtc);
        
        // Filter in Java to only include reservations that extend past hour start
        // This replaces the SQL DATE_ADD arithmetic for database compatibility
        return allReservedLanes.stream()
            .filter(row -> {
                LocalDateTime reservationStartUtc = (LocalDateTime) row[2];
                int durationHours = (int) row[3];
                LocalDateTime reservationEndUtc = reservationStartUtc.plusHours(durationHours);
                return reservationEndUtc.isAfter(hourStartUtc);
            })
            .collect(Collectors.toList());
    }
}
