package com.reservation_system.interfaces.dto;

import java.time.LocalTime;
import java.util.List;

public class AvailabilityResponse {
    
    private List<LaneDTO> availableLanes;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String message;
    
    public AvailabilityResponse() {}
    
    public AvailabilityResponse(List<LaneDTO> availableLanes, LocalTime openingTime, LocalTime closingTime, String message) {
        this.availableLanes = availableLanes;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.message = message;
    }
    
    public List<LaneDTO> getAvailableLanes() {
        return availableLanes;
    }
    
    public void setAvailableLanes(List<LaneDTO> availableLanes) {
        this.availableLanes = availableLanes;
    }
    
    public LocalTime getOpeningTime() {
        return openingTime;
    }
    
    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }
    
    public LocalTime getClosingTime() {
        return closingTime;
    }
    
    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
