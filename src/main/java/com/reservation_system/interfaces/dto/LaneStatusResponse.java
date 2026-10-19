package com.reservation_system.interfaces.dto;

import java.util.List;

/**
 * Response DTO for lane status showing available and reserved lanes for current hour.
 */
public class LaneStatusResponse {
    
    private List<Long> available;
    private List<ReservedLaneInfo> reserved;
    
    public LaneStatusResponse() {}
    
    public LaneStatusResponse(List<Long> available, List<ReservedLaneInfo> reserved) {
        this.available = available;
        this.reserved = reserved;
    }
    
    public List<Long> getAvailable() {
        return available;
    }
    
    public void setAvailable(List<Long> available) {
        this.available = available;
    }
    
    public List<ReservedLaneInfo> getReserved() {
        return reserved;
    }
    
    public void setReserved(List<ReservedLaneInfo> reserved) {
        this.reserved = reserved;
    }
}
