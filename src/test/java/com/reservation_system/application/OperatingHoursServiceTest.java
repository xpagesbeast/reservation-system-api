package com.reservation_system.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OperatingHoursServiceTest {
    
    @InjectMocks
    private OperatingHoursService operatingHoursService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(operatingHoursService, "operatingHours", 
                Map.of("spring", "12:00-00:00", "summer", "17:00-21:00", "fall", "12:00-00:00", "winter", "12:00-00:00"));
        ReflectionTestUtils.setField(operatingHoursService, "seasonDates", 
                Map.of("spring", "03-01,05-31", "summer", "06-01,08-31", "fall", "09-01,11-30", "winter", "12-01,02-28"));
    }
    
    @Test
    void getSeason_SpringDate_ReturnsSpring() {
        LocalDate date = LocalDate.of(2026, 4, 15);
        String season = operatingHoursService.getSeason(date);
        assertEquals("spring", season);
    }
    
    @Test
    void getSeason_SummerDate_ReturnsSummer() {
        LocalDate date = LocalDate.of(2026, 7, 15);
        String season = operatingHoursService.getSeason(date);
        assertEquals("summer", season);
    }
    
    @Test
    void getSeason_FallDate_ReturnsFall() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        String season = operatingHoursService.getSeason(date);
        assertEquals("fall", season);
    }
    
    @Test
    void getSeason_WinterDate_ReturnsWinter() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        String season = operatingHoursService.getSeason(date);
        assertEquals("winter", season);
    }
    
    @Test
    void getOpeningTime_Spring_ReturnsNoon() {
        LocalDate date = LocalDate.of(2026, 4, 15);
        LocalTime openingTime = operatingHoursService.getOpeningTime(date);
        assertEquals(LocalTime.of(12, 0), openingTime);
    }
    
    @Test
    void getOpeningTime_Summer_Returns5PM() {
        LocalDate date = LocalDate.of(2026, 7, 15);
        LocalTime openingTime = operatingHoursService.getOpeningTime(date);
        assertEquals(LocalTime.of(17, 0), openingTime);
    }
    
    @Test
    void isWithinOperatingHours_WithinHours_ReturnsTrue() {
        LocalDate date = LocalDate.of(2026, 4, 15);
        LocalTime time = LocalTime.of(18, 0);
        boolean withinHours = operatingHoursService.isWithinOperatingHours(date, time);
        assertTrue(withinHours);
    }
    
    @Test
    void isWithinOperatingHours_OutsideHours_ReturnsFalse() {
        LocalDate date = LocalDate.of(2026, 4, 15);
        LocalTime time = LocalTime.of(10, 0);
        boolean withinHours = operatingHoursService.isWithinOperatingHours(date, time);
        assertFalse(withinHours);
    }
}
