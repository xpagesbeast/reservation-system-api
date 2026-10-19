package com.reservation_system.interfaces.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityRequest {
    
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDate date;
    
    @NotNull(message = "Time is required")
    private LocalTime time;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;
    
    public AvailabilityRequest() {}
    
    public AvailabilityRequest(LocalDate date, LocalTime time, Integer durationHours) {
        this.date = date;
        this.time = time;
        this.durationHours = durationHours;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public void setTime(LocalTime time) {
        this.time = time;
    }
    
    public Integer getDurationHours() {
        return durationHours;
    }
    
    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }
}
