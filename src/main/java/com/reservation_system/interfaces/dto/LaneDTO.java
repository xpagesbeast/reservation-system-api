package com.reservation_system.interfaces.dto;

public class LaneDTO {
    
    private Long id;
    private Long laneNumber;
    
    public LaneDTO() {}
    
    public LaneDTO(Long id, Long laneNumber) {
        this.id = id;
        this.laneNumber = laneNumber;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getLaneNumber() {
        return laneNumber;
    }
    
    public void setLaneNumber(Long laneNumber) {
        this.laneNumber = laneNumber;
    }
}
