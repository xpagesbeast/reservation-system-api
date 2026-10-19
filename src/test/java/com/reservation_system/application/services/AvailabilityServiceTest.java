package com.reservation_system.application.services;

import com.reservation_system.application.OperatingHoursService;
import com.reservation_system.application.TimeZoneService;
import com.reservation_system.domain.Lane;
import com.reservation_system.infrastructure.persistence.LaneRepository;
import com.reservation_system.infrastructure.persistence.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {
    
    @Mock
    private LaneRepository laneRepository;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private OperatingHoursService operatingHoursService;
    
    @Mock
    private TimeZoneService timeZoneService;
    
    @InjectMocks
    private AvailabilityService availabilityService;
    
    private Lane lane1, lane2, lane3;
    
    @BeforeEach
    void setUp() {
        lane1 = new Lane(1);
        lane1.setId(1L);
        lane2 = new Lane(2);
        lane2.setId(2L);
        lane3 = new Lane(3);
        lane3.setId(3L);
    }
    
    @Test
    void getAvailableLanes_WithinOperatingHours_ReturnsAvailableLanes() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 8, 15);
        LocalTime time = LocalTime.of(18, 0);
        int durationHours = 1;
        
        when(operatingHoursService.isWithinOperatingHours(date, time)).thenReturn(true);
        when(timeZoneService.convertAspenToUtc(any(LocalDateTime.class))).thenReturn(java.time.LocalDateTime.of(2026, 8, 15, 0, 0));
        when(laneRepository.findByIsBlockedFalse()).thenReturn(Arrays.asList(lane1, lane2, lane3));
        when(reservationRepository.findOverlappingLanes(any(), any(), any())).thenReturn(Arrays.asList(lane3));
        
        // Act
        List<Lane> availableLanes = availabilityService.getAvailableLanes(date, time, durationHours);
        
        // Assert
        assertEquals(2, availableLanes.size());
        assertTrue(availableLanes.stream().anyMatch(l -> l.getId().equals(1L)));
        assertTrue(availableLanes.stream().anyMatch(l -> l.getId().equals(2L)));
        assertFalse(availableLanes.stream().anyMatch(l -> l.getId().equals(3L)));
    }
    
    @Test
    void getAvailableLanes_OutsideOperatingHours_ReturnsEmptyList() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 8, 15);
        LocalTime time = LocalTime.of(10, 0);
        int durationHours = 1;
        
        when(operatingHoursService.isWithinOperatingHours(date, time)).thenReturn(false);
        
        // Act
        List<Lane> availableLanes = availabilityService.getAvailableLanes(date, time, durationHours);
        
        // Assert
        assertTrue(availableLanes.isEmpty());
    }
    
    @Test
    void isLaneAvailable_LaneIsAvailable_ReturnsTrue() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 8, 15);
        LocalTime time = LocalTime.of(18, 0);
        int durationHours = 1;
        
        when(operatingHoursService.isWithinOperatingHours(date, time)).thenReturn(true);
        when(timeZoneService.convertAspenToUtc(any(LocalDateTime.class))).thenReturn(java.time.LocalDateTime.of(2026, 8, 15, 0, 0));
        when(laneRepository.findByIsBlockedFalse()).thenReturn(Arrays.asList(lane1));
        when(reservationRepository.findOverlappingLanes(any(), any(), any())).thenReturn(List.of());
        
        // Act
        boolean available = availabilityService.isLaneAvailable(1L, date, time, durationHours);
        
        // Assert
        assertTrue(available);
    }
}
