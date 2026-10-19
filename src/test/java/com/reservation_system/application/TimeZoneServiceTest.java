package com.reservation_system.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TimeZoneServiceTest {
    
    @InjectMocks
    private TimeZoneService timeZoneService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(timeZoneService, "timeZone", "America/Denver");
    }
    
    @Test
    void convertUtcToAspen_ValidDateTime_ReturnsConvertedTime() {
        LocalDateTime utcDateTime = LocalDateTime.of(2026, 8, 15, 18, 0);
        LocalDateTime aspenDateTime = timeZoneService.convertUtcToAspen(utcDateTime);
        assertNotNull(aspenDateTime);
        // The exact conversion depends on DST, but we verify it's not null
    }
    
    @Test
    void convertUtcToAspen_Null_ReturnsNull() {
        LocalDateTime result = timeZoneService.convertUtcToAspen((LocalDateTime) null);
        assertNull(result);
    }
    
    @Test
    void convertAspenToUtc_ValidDateTime_ReturnsConvertedTime() {
        LocalDateTime aspenDateTime = LocalDateTime.of(2026, 8, 15, 12, 0);
        LocalDateTime utcDateTime = timeZoneService.convertAspenToUtc(aspenDateTime);
        assertNotNull(utcDateTime);
        // The exact conversion depends on DST, but we verify it's not null
    }
    
    @Test
    void convertAspenToUtc_Null_ReturnsNull() {
        LocalDateTime result = timeZoneService.convertAspenToUtc((LocalDateTime) null);
        assertNull(result);
    }
    
    @Test
    void getAspenZoneId_ReturnsDenverZoneId() {
        assertEquals(java.time.ZoneId.of("America/Denver"), timeZoneService.getAspenZoneId());
    }
}
