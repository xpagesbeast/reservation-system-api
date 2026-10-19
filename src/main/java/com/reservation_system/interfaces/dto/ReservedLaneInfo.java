package com.reservation_system.interfaces.dto;

import java.time.LocalTime;

/**
 * DTO for reserved lane information including reservation details.
 */
public class ReservedLaneInfo {
    
    private Long laneId;
    private LocalTime until;
    private String customerName;
    
    public ReservedLaneInfo() {}
    
    public ReservedLaneInfo(Long laneId, LocalTime until, String customerName) {
        this.laneId = laneId;
        this.until = until;
        this.customerName = customerName;
    }
    
    public Long getLaneId() {
        return laneId;
    }
    
    public void setLaneId(Long laneId) {
        this.laneId = laneId;
    }
    
    public LocalTime getUntil() {
        return until;
    }
    
    public void setUntil(LocalTime until) {
        this.until = until;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
