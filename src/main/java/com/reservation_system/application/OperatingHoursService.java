package com.reservation_system.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@ConfigurationProperties(prefix = "reservation")
public class OperatingHoursService {
    
    private Map<String, String> operatingHours;
    private Map<String, String> seasonDates;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public Map<String, String> getOperatingHours() {
        return operatingHours;
    }
    
    public void setOperatingHours(Map<String, String> operatingHours) {
        this.operatingHours = operatingHours;
    }
    
    public Map<String, String> getSeasonDates() {
        return seasonDates;
    }
    
    public void setSeasonDates(Map<String, String> seasonDates) {
        this.seasonDates = seasonDates;
    }
    
    public String getSeason(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        
        for (Map.Entry<String, String> entry : seasonDates.entrySet()) {
            String[] range = entry.getValue().split(",");
            if (isDateInRange(dateStr, range[0], range[1])) {
                return entry.getKey();
            }
        }
        
        return "spring"; // default
    }
    
    private boolean isDateInRange(String date, String start, String end) {
        int dateValue = Integer.parseInt(date.replace("-", ""));
        int startValue = Integer.parseInt(start.replace("-", ""));
        int endValue = Integer.parseInt(end.replace("-", ""));
        
        if (startValue <= endValue) {
            return dateValue >= startValue && dateValue <= endValue;
        } else {
            // Handle year-end wrap-around (e.g., winter: Dec-Feb)
            return dateValue >= startValue || dateValue <= endValue;
        }
    }
    
    public LocalTime getOpeningTime(LocalDate date) {
        String season = getSeason(date);
        String hours = operatingHours.get(season);
        String[] times = hours.split("-");
        return LocalTime.parse(times[0], TIME_FORMATTER);
    }
    
    public LocalTime getClosingTime(LocalDate date) {
        String season = getSeason(date);
        String hours = operatingHours.get(season);
        String[] times = hours.split("-");
        return LocalTime.parse(times[1], TIME_FORMATTER);
    }
    
    public boolean isWithinOperatingHours(LocalDate date, LocalTime time) {
        LocalTime openingTime = getOpeningTime(date);
        LocalTime closingTime = getClosingTime(date);
        
        if (closingTime.equals(LocalTime.MIDNIGHT)) {
            return !time.isBefore(openingTime);
        }
        
        return !time.isBefore(openingTime) && !time.isAfter(closingTime);
    }
}
