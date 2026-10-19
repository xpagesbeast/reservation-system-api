package com.reservation_system.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class TimeZoneService {
    
    @Value("${reservation.time-zone}")
    private String timeZone;
    
    private final ZoneId aspenZoneId = ZoneId.of("America/Denver");
    
    public LocalDateTime convertUtcToAspen(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        ZonedDateTime utcZoned = utcDateTime.atZone(ZoneOffset.UTC);
        ZonedDateTime aspenZoned = utcZoned.withZoneSameInstant(aspenZoneId);
        return aspenZoned.toLocalDateTime();
    }
    
    public LocalDateTime convertAspenToUtc(LocalDateTime aspenDateTime) {
        if (aspenDateTime == null) {
            return null;
        }
        ZonedDateTime aspenZoned = aspenDateTime.atZone(aspenZoneId);
        ZonedDateTime utcZoned = aspenZoned.withZoneSameInstant(ZoneOffset.UTC);
        return utcZoned.toLocalDateTime();
    }
    
    public LocalDate convertUtcToAspen(LocalDate utcDate) {
        if (utcDate == null) {
            return null;
        }
        // Convert to start of day in UTC, then to Aspen time
        LocalDateTime utcDateTime = utcDate.atStartOfDay();
        LocalDateTime aspenDateTime = convertUtcToAspen(utcDateTime);
        return aspenDateTime.toLocalDate();
    }
    
    public LocalDate convertAspenToUtc(LocalDate aspenDate) {
        if (aspenDate == null) {
            return null;
        }
        // Convert to start of day in Aspen time, then to UTC
        LocalDateTime aspenDateTime = aspenDate.atStartOfDay();
        LocalDateTime utcDateTime = convertAspenToUtc(aspenDateTime);
        return utcDateTime.toLocalDate();
    }
    
    public ZoneId getAspenZoneId() {
        return aspenZoneId;
    }
    
    public String getTimeZone() {
        return timeZone;
    }
}
